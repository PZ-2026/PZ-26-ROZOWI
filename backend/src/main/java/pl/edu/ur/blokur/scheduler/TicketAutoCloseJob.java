package pl.edu.ur.blokur.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketHistory;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.repository.TicketHistoryRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.service.BusinessHoursCalculator;
import pl.edu.ur.blokur.service.TicketService;

/**
 * Job uruchamiany codziennie o godzinie 7:00, który wyszukuje zgłoszenia w stanie {@code
 * ZAKONCZONE_DO_WERYFIKACJI} i automatycznie je zamyka, jeśli od momentu ustawienia tego statusu
 * minęło co najmniej 5 dni roboczych bez zastrzeżeń ze strony mieszkańca.
 *
 * <p>Specyfikacja (KA-04) stanowi: „Mieszkaniec ma 5 dni roboczych na zgłoszenie zastrzeżeń.
 * Zarządca potwierdza zamknięcie zgłoszenia (po weryfikacji lub po upływie 5 dni roboczych bez
 * zastrzeżeń)."
 */
@Component
public class TicketAutoCloseJob {

    private static final Logger log = LoggerFactory.getLogger(TicketAutoCloseJob.class);

    /** Liczba dni roboczych, po których zgłoszenie jest zamykane automatycznie. */
    static final int VERIFICATION_BUSINESS_DAYS = 5;

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final BusinessHoursCalculator businessHoursCalculator;
    private final TicketService ticketService;

    /**
     * Tworzy instancję joba z wymaganymi zależnościami.
     *
     * @param ticketRepository repozytorium zgłoszeń
     * @param ticketHistoryRepository repozytorium historii zgłoszeń
     * @param businessHoursCalculator kalkulator godzin roboczych
     * @param ticketService serwis zgłoszeń (dostarcza metodę zamknięcia)
     */
    public TicketAutoCloseJob(
            TicketRepository ticketRepository,
            TicketHistoryRepository ticketHistoryRepository,
            BusinessHoursCalculator businessHoursCalculator,
            TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.businessHoursCalculator = businessHoursCalculator;
        this.ticketService = ticketService;
    }

    /**
     * Główna metoda joba uruchamiana codziennie o 7:00. Wyszukuje wszystkie zgłoszenia w stanie
     * {@code ZAKONCZONE_DO_WERYFIKACJI} i zamyka te, dla których upłynęło {@value
     * #VERIFICATION_BUSINESS_DAYS} dni roboczych.
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void autoCloseExpiredTickets() {
        List<Ticket> candidates =
                ticketRepository.findAllByTicketStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI);

        if (candidates.isEmpty()) {
            return;
        }

        log.info(
                "TicketAutoCloseJob: sprawdzam {} zgłoszenia w statusie ZAKONCZONE_DO_WERYFIKACJI.",
                candidates.size());

        for (Ticket ticket : candidates) {
            processCandidate(ticket);
        }
    }

    /**
     * Sprawdza pojedyncze zgłoszenie i zamyka je, jeśli minęło wystarczająco dużo godzin
     * roboczych od momentu wejścia w stan {@code ZAKONCZONE_DO_WERYFIKACJI}.
     *
     * @param ticket zgłoszenie kandydat do automatycznego zamknięcia
     */
    void processCandidate(Ticket ticket) {
        List<TicketHistory> entries =
                ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                        ticket.getId(), "ZAKONCZONE_DO_WERYFIKACJI");

        if (entries.isEmpty()) {
            log.warn(
                    "Brak wpisu historii dla statusu ZAKONCZONE_DO_WERYFIKACJI w zgłoszeniu {}."
                            + " Pomijam.",
                    ticket.getTicketNumber());
            return;
        }

        LocalDateTime enteredVerificationAt = entries.get(0).getCreatedAt();
        double elapsedBusinessHours =
                businessHoursCalculator.calculate(enteredVerificationAt, LocalDateTime.now());
        double thresholdHours = VERIFICATION_BUSINESS_DAYS * hoursPerDay();

        if (elapsedBusinessHours >= thresholdHours) {
            log.info(
                    "Automatyczne zamykanie zgłoszenia {} po {}/{} godzinach roboczych.",
                    ticket.getTicketNumber(),
                    String.format("%.1f", elapsedBusinessHours),
                    thresholdHours);
            try {
                ticketService.autoCloseTicket(ticket.getId());
            } catch (Exception e) {
                log.error(
                        "Błąd podczas auto-zamknięcia zgłoszenia {}: {}",
                        ticket.getTicketNumber(),
                        e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Zwraca liczbę godzin roboczych w jednym dniu roboczym. Wyznaczana na podstawie konfiguracji
     * kalkulatora SLA ({@code sla.work-start} i {@code sla.work-end}). Używana do obliczenia
     * progu godzin roboczych odpowiadającego {@value #VERIFICATION_BUSINESS_DAYS} dniom.
     *
     * @return liczba godzin roboczych w dobie (np. 8 dla konfiguracji 8:00–16:00)
     */
    int hoursPerDay() {
        /*
         * Obliczamy pośrednio: kalkulator liczy godziny od np. 8:00 do 16:00.
         * Jeden dzień roboczy = calculate(poniedziałek 8:00, poniedziałek 16:00).
         * Zamiast duplikować pola prywatne, obliczamy referencyjnie dla dowolnego pon-pt.
         */
        LocalDateTime refStart = java.time.LocalDate.of(2024, 1, 1).atTime(0, 0); // poniedziałek
        LocalDateTime refEnd = java.time.LocalDate.of(2024, 1, 1).atTime(23, 59);
        return (int) businessHoursCalculator.calculate(refStart, refEnd);
    }
}

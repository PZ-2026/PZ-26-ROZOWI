package pl.edu.ur.blokur.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.ur.blokur.models.Inspection;
import pl.edu.ur.blokur.repository.InspectionRepository;

/**
 * Job uruchamiany codziennie o 8:00, który wykrywa przeglądy techniczne zaplanowane za dokładnie 7
 * dni lub 24 godziny i wysyła powiadomienia do mieszkańców w zakresie danego przeglądu.
 */
@Component
public class InspectionReminderJob {

    private static final Logger log = LoggerFactory.getLogger(InspectionReminderJob.class);

    private final InspectionRepository inspectionRepository;

    /**
     * Tworzy instancję joba z wymaganym repozytorium przeglądów.
     *
     * @param inspectionRepository repozytorium przeglądów technicznych
     */
    public InspectionReminderJob(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
    }

    /**
     * Główna metoda joba uruchamiana codziennie o 8:00. Wyszukuje przeglądy zaplanowane na następny
     * dzień (przypomnienie 24h) oraz za 7 dni i inicjuje wysyłkę powiadomień PUSH do mieszkańców w
     * zasięgu danego przeglądu.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        LocalDate today = LocalDate.now();

        sendRemindersForWindow(today.plusDays(1), "24h");
        sendRemindersForWindow(today.plusDays(7), "7 dni");
    }

    /**
     * Pobiera przeglądy zaplanowane na podany dzień i inicjuje wysyłkę powiadomień.
     *
     * @param targetDate dzień, dla którego wyszukujemy przeglądy
     * @param label etykieta okna czasowego (do logowania)
     */
    private void sendRemindersForWindow(LocalDate targetDate, String label) {
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();

        List<Inspection> upcoming = inspectionRepository.findByScheduledAtBetween(from, to);

        if (upcoming.isEmpty()) {
            return;
        }

        log.info(
                "Przypomnienie ({}): znaleziono {} przeglądów na {}.",
                label,
                upcoming.size(),
                targetDate);

        for (Inspection inspection : upcoming) {
            sendPushNotification(inspection, label);
        }
    }

    /**
     * Wysyła powiadomienie PUSH do mieszkańców w zasięgu przeglądu.
     *
     * @param inspection przegląd, dla którego wysyłane jest powiadomienie
     * @param timeLabel etykieta czasowa przypomnienia (np. "24h", "7 dni")
     */
    private void sendPushNotification(Inspection inspection, String timeLabel) {
        // TODO: zaimplementować wysyłkę powiadomień PUSH do mieszkańców w zasięgu przeglądu.
        // Zasięg określony przez inspection.getScopeType() i inspection.getScopeId().
        // Powiadomienie: "Przegląd '%s' zaplanowany za %s (%s)".
        log.debug(
                "TODO PUSH: przegląd='{}', zasięg={}:{}, za={}",
                inspection.getTitle(),
                inspection.getScopeType(),
                inspection.getScopeId(),
                timeLabel);
    }
}

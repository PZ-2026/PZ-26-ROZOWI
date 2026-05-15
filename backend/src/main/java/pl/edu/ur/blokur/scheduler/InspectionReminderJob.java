package pl.edu.ur.blokur.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.models.Inspection;
import pl.edu.ur.blokur.models.ScopeType;
import pl.edu.ur.blokur.repository.InspectionRepository;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.PushNotificationService;

/**
 * Job uruchamiany codziennie o 8:00, który wykrywa przeglądy techniczne zaplanowane za dokładnie 7
 * dni lub 24 godziny i wysyła powiadomienia do mieszkańców w zakresie danego przeglądu.
 */
@Component
public class InspectionReminderJob {

    private static final Logger log = LoggerFactory.getLogger(InspectionReminderJob.class);

    private final InspectionRepository inspectionRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    /**
     * Tworzy instancję joba z wymaganymi zależnościami.
     *
     * @param inspectionRepository repozytorium przeglądów technicznych
     * @param userRepository repozytorium użytkowników
     * @param pushNotificationService serwis powiadomień PUSH
     */
    public InspectionReminderJob(
            InspectionRepository inspectionRepository,
            UserRepository userRepository,
            PushNotificationService pushNotificationService) {
        this.inspectionRepository = inspectionRepository;
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
    }

    /**
     * Główna metoda joba uruchamiana codziennie o 8:00. Wyszukuje przeglądy zaplanowane na następny
     * dzień (przypomnienie 24h) oraz za 7 dni i inicjuje wysyłkę powiadomień PUSH do mieszkańców w
     * zasięgu danego przeglądu.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        LocalDate today = LocalDate.now();

        sendRemindersForWindow(
                today.plusDays(1), "24h", Inspection::isNotified24h, i -> i.setNotified24h(true));
        sendRemindersForWindow(
                today.plusDays(7), "7 dni", Inspection::isNotified7d, i -> i.setNotified7d(true));
    }

    /**
     * Pobiera przeglądy zaplanowane na podany dzień i inicjuje wysyłkę powiadomień. Pomija
     * przeglądy, dla których flaga idempotentności jest już ustawiona.
     *
     * @param targetDate dzień, dla którego wyszukujemy przeglądy
     * @param label etykieta okna czasowego (do logowania)
     * @param alreadyNotified predykat sprawdzający flagę idempotentności
     * @param markNotified konsumer ustawiający flagę po wysłaniu
     */
    private void sendRemindersForWindow(
            LocalDate targetDate,
            String label,
            Predicate<Inspection> alreadyNotified,
            Consumer<Inspection> markNotified) {
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
            if (alreadyNotified.test(inspection)) {
                log.debug(
                        "Przegląd '{}' — powiadomienie {} już wysłane, pomijam.",
                        inspection.getTitle(),
                        label);
                continue;
            }
            sendPushNotification(inspection, label, markNotified);
        }
    }

    /**
     * Wysyła powiadomienie PUSH do mieszkańców w zasięgu przeglądu i ustawia flagę idempotentności.
     *
     * @param inspection przegląd, dla którego wysyłane jest powiadomienie
     * @param timeLabel etykieta czasowa przypomnienia (np. "24h", "7 dni")
     * @param markNotified konsumer ustawiający flagę po wysłaniu
     */
    @Transactional
    void sendPushNotification(
            Inspection inspection, String timeLabel, Consumer<Inspection> markNotified) {
        List<UUID> recipientIds = resolveRecipientIds(inspection);
        if (recipientIds.isEmpty()) {
            log.debug(
                    "Brak odbiorców dla przeglądu '{}' (zasięg={}:{})",
                    inspection.getTitle(),
                    inspection.getScopeType(),
                    inspection.getScopeId());
            return;
        }

        String title = "Przypomnienie o przeglądzie";
        String body =
                String.format(
                        "Przegląd '%s' zaplanowany za %s (%s).",
                        inspection.getTitle(),
                        timeLabel,
                        inspection.getScheduledAt().toLocalDate());
        Map<String, String> data =
                Map.of(
                        "inspectionId",
                        inspection.getId().toString(),
                        "scheduledAt",
                        inspection.getScheduledAt().toString());

        pushNotificationService.sendToUsers(
                recipientIds, PushNotificationService.EVENT_PRZEGLAD, title, body, data);

        markNotified.accept(inspection);
        inspectionRepository.save(inspection);
    }

    private List<UUID> resolveRecipientIds(Inspection inspection) {
        ScopeType scopeType = inspection.getScopeType();
        UUID scopeId = inspection.getScopeId();
        if (scopeType == null || scopeId == null) {
            return List.of();
        }
        switch (scopeType) {
            case BUDYNEK:
                return userRepository.findUserIdsByBuildingId(scopeId);
            case KLATKA:
                return userRepository.findUserIdsByStaircaseId(scopeId);
            case NIERUCHOMOSC:
                return userRepository.findUserIdsByPropertyId(scopeId);
            default:
                return List.of();
        }
    }
}

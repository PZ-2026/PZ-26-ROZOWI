package pl.edu.ur.blokur.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.models.Inspection;
import pl.edu.ur.blokur.models.ScopeType;
import pl.edu.ur.blokur.repository.InspectionRepository;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.PushNotificationService;

/**
 * Testy jednostkowe dla {@link InspectionReminderJob}. Weryfikują poprawność doboru odbiorców
 * powiadomień na podstawie scopeType przeglądu, obsługę pustych okien czasowych oraz idempotentność
 * (flagi notified_24h / notified_7d).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InspectionReminderJob — przypomnienia o przeglądach")
class InspectionReminderJobTest {

    @Mock private InspectionRepository inspectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PushNotificationService pushNotificationService;

    @InjectMocks private InspectionReminderJob reminderJob;

    private UUID scopeId;

    @BeforeEach
    void setUp() {
        scopeId = UUID.randomUUID();
    }

    private Inspection buildInspection(ScopeType scopeType) {
        Inspection inspection = new Inspection();
        inspection.setId(UUID.randomUUID());
        inspection.setTitle("Przegląd testowy");
        inspection.setScopeType(scopeType);
        inspection.setScopeId(scopeId);
        inspection.setScheduledAt(LocalDateTime.now().plusDays(1));
        return inspection;
    }

    // =======================================================
    // Brak przeglądów
    // =======================================================

    @Nested
    @DisplayName("Brak przeglądów w oknie czasowym")
    class NoPendingInspectionsTests {

        @Test
        @DisplayName("Brak przeglądów — nie wysyła żadnych powiadomień PUSH")
        void shouldNotSendPushWhenNoInspections() {
            when(inspectionRepository.findByScheduledAtBetween(any(), any())).thenReturn(List.of());

            reminderJob.sendReminders();

            verify(pushNotificationService, never())
                    .sendToUsers(anyList(), anyString(), anyString(), anyString(), anyMap());
        }
    }

    // =======================================================
    // Zasięg BUDYNEK
    // =======================================================

    @Nested
    @DisplayName("Przegląd o zasięgu BUDYNEK")
    class BuildingScopeTests {

        @Test
        @DisplayName("Przegląd BUDYNEK — wywołuje findUserIdsByBuildingId i wysyła PUSH")
        void shouldSendToUsersInBuilding() {
            Inspection inspection = buildInspection(ScopeType.BUDYNEK);
            List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());
            when(userRepository.findUserIdsByBuildingId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(userRepository).findUserIdsByBuildingId(scopeId);
            verify(pushNotificationService)
                    .sendToUsers(
                            eq(userIds),
                            eq(PushNotificationService.EVENT_PRZEGLAD),
                            anyString(),
                            anyString(),
                            anyMap());
        }
    }

    // =======================================================
    // Zasięg KLATKA
    // =======================================================

    @Nested
    @DisplayName("Przegląd o zasięgu KLATKA")
    class StaircaseScopeTests {

        @Test
        @DisplayName("Przegląd KLATKA — wywołuje findUserIdsByStaircaseId i wysyła PUSH")
        void shouldSendToUsersInStaircase() {
            Inspection inspection = buildInspection(ScopeType.KLATKA);
            List<UUID> userIds = List.of(UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());
            when(userRepository.findUserIdsByStaircaseId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(userRepository).findUserIdsByStaircaseId(scopeId);
            verify(pushNotificationService)
                    .sendToUsers(
                            eq(userIds),
                            eq(PushNotificationService.EVENT_PRZEGLAD),
                            anyString(),
                            anyString(),
                            anyMap());
        }
    }

    // =======================================================
    // Zasięg NIERUCHOMOSC
    // =======================================================

    @Nested
    @DisplayName("Przegląd o zasięgu NIERUCHOMOSC")
    class PropertyScopeTests {

        @Test
        @DisplayName("Przegląd NIERUCHOMOSC — wywołuje findUserIdsByPropertyId i wysyła PUSH")
        void shouldSendToUsersInProperty() {
            Inspection inspection = buildInspection(ScopeType.NIERUCHOMOSC);
            List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());
            when(userRepository.findUserIdsByPropertyId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(userRepository).findUserIdsByPropertyId(scopeId);
            verify(pushNotificationService)
                    .sendToUsers(
                            eq(userIds),
                            eq(PushNotificationService.EVENT_PRZEGLAD),
                            anyString(),
                            anyString(),
                            anyMap());
        }
    }

    // =======================================================
    // Brak odbiorców
    // =======================================================

    @Nested
    @DisplayName("Brak odbiorców dla przeglądu")
    class NoRecipientsTests {

        @Test
        @DisplayName("Pusta lista userIds — nie wywołuje pushNotificationService")
        void shouldNotSendWhenNoRecipientsForInspection() {
            Inspection inspection = buildInspection(ScopeType.BUDYNEK);

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());
            when(userRepository.findUserIdsByBuildingId(scopeId)).thenReturn(List.of());

            reminderJob.sendReminders();

            verify(pushNotificationService, never())
                    .sendToUsers(anyList(), anyString(), anyString(), anyString(), anyMap());
        }
    }

    // =======================================================
    // Dwa okna czasowe (24h i 7 dni)
    // =======================================================

    @Nested
    @DisplayName("Dwa okna czasowe")
    class TwoTimeWindowsTests {

        @Test
        @DisplayName("Przeglądy w obu oknach (24h i 7 dni) — PUSH wysłany dla każdego przeglądu")
        void shouldSendForBothTimeWindows() {
            Inspection inspection24h = buildInspection(ScopeType.BUDYNEK);
            Inspection inspection7d = buildInspection(ScopeType.KLATKA);
            List<UUID> userIds = List.of(UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection24h))
                    .thenReturn(List.of(inspection7d));
            when(userRepository.findUserIdsByBuildingId(scopeId)).thenReturn(userIds);
            when(userRepository.findUserIdsByStaircaseId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(pushNotificationService, times(2))
                    .sendToUsers(
                            eq(userIds),
                            eq(PushNotificationService.EVENT_PRZEGLAD),
                            anyString(),
                            anyString(),
                            anyMap());
        }
    }

    // =======================================================
    // Idempotentność (flagi notified_24h / notified_7d)
    // =======================================================

    @Nested
    @DisplayName("Idempotentność — flagi notified_24h / notified_7d")
    class IdempotencyTests {

        @Test
        @DisplayName("Przegląd z notified_24h=true — pomija wysyłkę i nie zapisuje ponownie")
        void shouldSkipAlreadyNotified24h() {
            Inspection inspection = buildInspection(ScopeType.BUDYNEK);
            inspection.setNotified24h(true);

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());

            reminderJob.sendReminders();

            verify(pushNotificationService, never())
                    .sendToUsers(anyList(), anyString(), anyString(), anyString(), anyMap());
            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Przegląd z notified_7d=true — pomija wysyłkę i nie zapisuje ponownie")
        void shouldSkipAlreadyNotified7d() {
            Inspection inspection = buildInspection(ScopeType.KLATKA);
            inspection.setNotified7d(true);

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of())
                    .thenReturn(List.of(inspection));

            reminderJob.sendReminders();

            verify(pushNotificationService, never())
                    .sendToUsers(anyList(), anyString(), anyString(), anyString(), anyMap());
            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Przegląd z notified_24h=false — po wysłaniu zapisuje inspection z flagą true")
        void shouldMarkNotified24hAfterSending() {
            Inspection inspection = buildInspection(ScopeType.BUDYNEK);
            List<UUID> userIds = List.of(UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of(inspection))
                    .thenReturn(List.of());
            when(userRepository.findUserIdsByBuildingId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(inspectionRepository).save(inspection);
            org.assertj.core.api.Assertions.assertThat(inspection.isNotified24h()).isTrue();
        }

        @Test
        @DisplayName("Przegląd z notified_7d=false — po wysłaniu zapisuje inspection z flagą true")
        void shouldMarkNotified7dAfterSending() {
            Inspection inspection = buildInspection(ScopeType.KLATKA);
            List<UUID> userIds = List.of(UUID.randomUUID());

            when(inspectionRepository.findByScheduledAtBetween(any(), any()))
                    .thenReturn(List.of())
                    .thenReturn(List.of(inspection));
            when(userRepository.findUserIdsByStaircaseId(scopeId)).thenReturn(userIds);

            reminderJob.sendReminders();

            verify(inspectionRepository).save(inspection);
            org.assertj.core.api.Assertions.assertThat(inspection.isNotified7d()).isTrue();
        }
    }
}

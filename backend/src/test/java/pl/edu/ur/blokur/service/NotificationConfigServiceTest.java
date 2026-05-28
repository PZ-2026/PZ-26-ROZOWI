package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.NotificationConfig;
import pl.edu.ur.blokur.repository.NotificationConfigRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConfigService — konfiguracja globalnych powiadomień PUSH")
class NotificationConfigServiceTest {

    @Mock private NotificationConfigRepository notificationConfigRepository;

    @InjectMocks private NotificationConfigService notificationConfigService;

    private NotificationConfig makeConfig(String eventType, boolean enabled) {
        NotificationConfig c = new NotificationConfig();
        c.setEventType(eventType);
        c.setEnabled(enabled);
        return c;
    }

    @Nested
    @DisplayName("getAll — pobieranie wszystkich konfiguracji")
    class GetAllTests {

        @Test
        @DisplayName("Zwraca listę DTO dla wszystkich konfiguracji")
        void shouldReturnAllConfigsAsDtos() {
            when(notificationConfigRepository.findAll()).thenReturn(List.of(
                    makeConfig("OGLOSZENIE", true),
                    makeConfig("PRZEGLAD", false)));

            List<NotificationConfigResponse> result = notificationConfigService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEventType()).isEqualTo("OGLOSZENIE");
            assertThat(result.get(0).isEnabled()).isTrue();
            assertThat(result.get(0).getLabel()).isEqualTo("Ogłoszenia");
            assertThat(result.get(1).getEventType()).isEqualTo("PRZEGLAD");
            assertThat(result.get(1).isEnabled()).isFalse();
            assertThat(result.get(1).getLabel()).isEqualTo("Przeglądy techniczne");
        }

        @Test
        @DisplayName("Zwraca pustą listę gdy brak konfiguracji")
        void shouldReturnEmptyListWhenNoConfigs() {
            when(notificationConfigRepository.findAll()).thenReturn(List.of());

            List<NotificationConfigResponse> result = notificationConfigService.getAll();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Używa nazwy eventType jako etykiety gdy brak mapowania")
        void shouldUseEventTypeAsLabelWhenNoMapping() {
            when(notificationConfigRepository.findAll()).thenReturn(List.of(
                    makeConfig("NIEZNANY_TYP", true)));

            List<NotificationConfigResponse> result = notificationConfigService.getAll();

            assertThat(result.get(0).getLabel()).isEqualTo("NIEZNANY_TYP");
        }
    }

    @Nested
    @DisplayName("update — aktualizacja flagi enabled")
    class UpdateTests {

        @Test
        @DisplayName("Aktualizuje flagę i zwraca zaktualizowane DTO")
        void shouldUpdateEnabledFlagAndReturnDto() {
            NotificationConfig config = makeConfig("OGLOSZENIE", true);
            NotificationConfig saved = makeConfig("OGLOSZENIE", false);

            when(notificationConfigRepository.findByEventType("OGLOSZENIE"))
                    .thenReturn(Optional.of(config));
            when(notificationConfigRepository.save(config)).thenReturn(saved);

            NotificationConfigResponse result =
                    notificationConfigService.update("OGLOSZENIE", false);

            assertThat(result.isEnabled()).isFalse();
            assertThat(result.getEventType()).isEqualTo("OGLOSZENIE");
            verify(notificationConfigRepository).save(config);
        }

        @Test
        @DisplayName("Rzuca NotFoundException gdy eventType nie istnieje")
        void shouldThrowNotFoundWhenEventTypeDoesNotExist() {
            when(notificationConfigRepository.findByEventType("NIEISTNIEJACY"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationConfigService.update("NIEISTNIEJACY", true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("NIEISTNIEJACY");
        }

        @Test
        @DisplayName("Etykieta NOWY_DOKUMENT jest tłumaczona poprawnie")
        void shouldTranslateNovyDokumentLabel() {
            NotificationConfig config = makeConfig("NOWY_DOKUMENT", false);
            when(notificationConfigRepository.findByEventType("NOWY_DOKUMENT"))
                    .thenReturn(Optional.of(config));
            when(notificationConfigRepository.save(any())).thenReturn(config);

            NotificationConfigResponse result =
                    notificationConfigService.update("NOWY_DOKUMENT", false);

            assertThat(result.getLabel()).isEqualTo("Nowe dokumenty");
        }
    }
}

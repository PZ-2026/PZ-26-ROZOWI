package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.NotificationSetting;

/** Repozytorium JPA dla encji {@link NotificationSetting}. */
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, UUID> {

    /**
     * Wyszukuje ustawienie powiadomień dla konkretnego użytkownika i typu zdarzenia.
     *
     * @param userId identyfikator użytkownika
     * @param eventType typ zdarzenia (np. OGLOSZENIE, PRZEGLAD)
     * @return opcjonalne ustawienie; jeśli brak — powiadomienie jest domyślnie włączone
     */
    Optional<NotificationSetting> findByUserIdAndEventType(UUID userId, String eventType);

    /**
     * Zwraca wszystkie ustawienia powiadomień dla danego użytkownika.
     *
     * @param userId identyfikator użytkownika
     * @return lista ustawień
     */
    List<NotificationSetting> findByUserId(UUID userId);
}

package pl.edu.ur.blokur.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.NotificationConfig;

/** Repozytorium JPA dla encji {@link NotificationConfig}. */
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, String> {

    /**
     * Wyszukuje globalną konfigurację dla podanego typu zdarzenia.
     *
     * @param eventType typ zdarzenia (np. {@code "OGLOSZENIE"})
     * @return opcjonalna konfiguracja; pusta gdy brak rekordu (traktowane jako włączone)
     */
    Optional<NotificationConfig> findByEventType(String eventType);
}

package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja przechowująca ustawienia powiadomień PUSH dla użytkownika i danego typu zdarzenia. */
@Entity
@Table(name = "notification_settings")
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Zwraca unikalny identyfikator rekordu ustawienia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator rekordu ustawienia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator użytkownika, do którego należy ustawienie.
     *
     * @return UUID użytkownika
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Ustawia identyfikator użytkownika, do którego należy ustawienie.
     *
     * @param userId UUID użytkownika
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Zwraca typ zdarzenia, którego dotyczy ustawienie (np. OGLOSZENIE, PRZEGLAD).
     *
     * @return typ zdarzenia
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Ustawia typ zdarzenia, którego dotyczy ustawienie.
     *
     * @param eventType typ zdarzenia
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Informuje, czy powiadomienia PUSH dla danego typu zdarzenia są włączone.
     *
     * @return {@code true} jeśli powiadomienia są włączone
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Włącza lub wyłącza powiadomienia PUSH dla danego typu zdarzenia.
     *
     * @param enabled {@code false} aby wyłączyć powiadomienia tego typu
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

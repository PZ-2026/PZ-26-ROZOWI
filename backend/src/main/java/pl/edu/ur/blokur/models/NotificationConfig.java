package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Globalna konfiguracja typów powiadomień PUSH zarządzana przez zarządcę. Każdy rekord odpowiada
 * jednemu typowi zdarzenia i przechowuje flagę włączenia/wyłączenia dla całego systemu.
 */
@Entity
@Table(name = "notification_config")
public class NotificationConfig {

    @Id
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Zwraca typ zdarzenia (klucz główny).
     *
     * @return typ zdarzenia, np. {@code "OGLOSZENIE"}
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Ustawia typ zdarzenia.
     *
     * @param eventType typ zdarzenia
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Zwraca informację, czy powiadomienia tego typu są globalnie włączone.
     *
     * @return {@code true} jeśli typ powiadomienia jest aktywny
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Ustawia flagę globalnego włączenia/wyłączenia powiadomień danego typu.
     *
     * @param enabled {@code true} aby włączyć, {@code false} aby wyłączyć
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

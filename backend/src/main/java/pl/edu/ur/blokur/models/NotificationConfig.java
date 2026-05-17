package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Globalna konfiguracja typów powiadomień PUSH zarządzana przez zarządcę. Każdy rekord odpowiada
 * jednemu typowi zdarzenia i przechowuje flagę włączenia/wyłączenia dla całego systemu.
 */
@Entity
@Table(name = "notification_config")
@Getter
@Setter
@NoArgsConstructor
public class NotificationConfig {

    @Id
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}

package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/** Encja reprezentująca urządzenie użytkownika z tokenem FCM do powiadomień PUSH. */
@Entity
@Table(name = "user_devices")
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", unique = true, nullable = false, length = 255)
    private String fcmToken;

    @Column(name = "platform", length = 20)
    private String platform;

    @CreationTimestamp
    @Column(name = "created_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * Zwraca unikalny identyfikator rekordu urządzenia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator rekordu urządzenia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator użytkownika będącego właścicielem urządzenia.
     *
     * @return UUID użytkownika
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Ustawia identyfikator użytkownika będącego właścicielem urządzenia.
     *
     * @param userId UUID użytkownika
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Zwraca token FCM urządzenia używany do wysyłania powiadomień PUSH.
     *
     * @return token FCM
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Ustawia token FCM urządzenia.
     *
     * @param fcmToken token FCM
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Zwraca platformę urządzenia (np. ANDROID, IOS).
     *
     * @return nazwa platformy lub {@code null} jeśli nie podano
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Ustawia platformę urządzenia.
     *
     * @param platform nazwa platformy (np. ANDROID, IOS)
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Zwraca datę i czas rejestracji urządzenia.
     *
     * @return data i czas rejestracji
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas rejestracji urządzenia.
     *
     * @param createdAt data i czas rejestracji
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

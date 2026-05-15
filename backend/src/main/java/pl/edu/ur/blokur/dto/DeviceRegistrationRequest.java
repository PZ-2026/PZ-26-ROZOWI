package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO żądania rejestracji urządzenia mobilnego z tokenem FCM. */
public class DeviceRegistrationRequest {

    @NotBlank(message = "Token FCM jest wymagany")
    @Size(max = 255)
    private String fcmToken;

    @Size(max = 20)
    private String platform;

    /**
     * Zwraca token FCM urządzenia mobilnego.
     *
     * @return token FCM
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Ustawia token FCM urządzenia mobilnego.
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
}

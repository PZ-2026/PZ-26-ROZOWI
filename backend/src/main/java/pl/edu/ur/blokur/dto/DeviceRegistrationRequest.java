package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO żądania rejestracji urządzenia mobilnego z tokenem FCM. */
@Data
public class DeviceRegistrationRequest {

    @NotBlank(message = "Token FCM jest wymagany")
    @Size(max = 255)
    private String fcmToken;

    @Size(max = 20)
    private String platform;
}

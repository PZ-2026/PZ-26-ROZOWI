package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.DeviceRegistrationRequest;
import pl.edu.ur.blokur.service.UserDeviceService;

/** Kontroler obsługujący rejestrację i wyrejestrowanie tokenów FCM urządzeń mobilnych. */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final UserDeviceService userDeviceService;

    public DeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    /**
     * Rejestruje token FCM urządzenia mobilnego dla zalogowanego użytkownika.
     *
     * @param request dane urządzenia (token FCM, platforma)
     * @param principal zalogowany użytkownik
     * @return 204 No Content po pomyślnej rejestracji
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        userDeviceService.registerDevice(userId, request.getFcmToken(), request.getPlatform());
        return ResponseEntity.noContent().build();
    }

    /**
     * Usuwa token FCM urządzenia mobilnego zalogowanego użytkownika.
     *
     * @param token token FCM do usunięcia
     * @param principal zalogowany użytkownik
     * @return 204 No Content po pomyślnym usunięciu
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregisterDevice(@PathVariable String token, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        userDeviceService.unregisterDevice(userId, token);
        return ResponseEntity.noContent().build();
    }
}

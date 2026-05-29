package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.DeviceRegistrationRequest;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.UserDeviceService;

/** Kontroler obsługujący rejestrację i wyrejestrowanie tokenów FCM urządzeń mobilnych. */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final UserDeviceService userDeviceService;
    private final UserRepository userRepository;

    public DeviceController(UserDeviceService userDeviceService, UserRepository userRepository) {
        this.userDeviceService = userDeviceService;
        this.userRepository = userRepository;
    }

    /**
     * Rejestruje token FCM urządzenia mobilnego dla zalogowanego użytkownika.
     *
     * <p>JWT subject zawiera adres email (nie UUID), dlatego użytkownik jest wyszukiwany
     * po emailu zamiast bezpośrednio przez UUID.
     *
     * @param request dane urządzenia (token FCM, platforma)
     * @param principal zalogowany użytkownik (subject = email)
     * @return 204 No Content po pomyślnej rejestracji, 404 gdy użytkownik nie istnieje
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request, Principal principal) {
        User user = userRepository
                .findByEmail(principal.getName())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userDeviceService.registerDevice(user.getId(), request.getFcmToken(), request.getPlatform());
        return ResponseEntity.noContent().build();
    }

    /**
     * Usuwa token FCM urządzenia mobilnego zalogowanego użytkownika.
     *
     * <p>JWT subject zawiera adres email (nie UUID), dlatego użytkownik jest wyszukiwany
     * po emailu zamiast bezpośrednio przez UUID.
     *
     * @param token token FCM do usunięcia
     * @param principal zalogowany użytkownik (subject = email)
     * @return 204 No Content po pomyślnym usunięciu, 404 gdy użytkownik nie istnieje
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregisterDevice(@PathVariable String token, Principal principal) {
        User user = userRepository
                .findByEmail(principal.getName())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userDeviceService.unregisterDevice(user.getId(), token);
        return ResponseEntity.noContent().build();
    }
}

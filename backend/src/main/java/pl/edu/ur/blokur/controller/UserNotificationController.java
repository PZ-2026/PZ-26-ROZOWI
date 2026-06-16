package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.dto.UpdateNotificationConfigRequest;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.UserNotificationService;

/**
 * Kontroler do zarządzania osobistymi ustawieniami powiadomień.
 * Dostępny dla każdego zalogowanego użytkownika.
 */
@RestController
@RequestMapping("/api/notifications/settings")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final UserRepository userRepository;

    public UserNotificationController(
            UserNotificationService userNotificationService, UserRepository userRepository) {
        this.userNotificationService = userNotificationService;
        this.userRepository = userRepository;
    }

    /**
     * Zwraca listę wszystkich osobistych ustawień powiadomień zalogowanego użytkownika.
     */
    @GetMapping
    public ResponseEntity<List<NotificationConfigResponse>> getMySettings(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userNotificationService.getSettingsForUser(user.getId()));
    }

    /**
     * Zmienia stan ustawienia powiadomienia dla podanego typu zdarzenia.
     */
    @PatchMapping("/{eventType}")
    public ResponseEntity<?> updateMySetting(
            @PathVariable String eventType,
            @Valid @RequestBody UpdateNotificationConfigRequest request,
            Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            return ResponseEntity.ok(
                    userNotificationService.updateSettingForUser(
                            user.getId(), eventType, request.getEnabled()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

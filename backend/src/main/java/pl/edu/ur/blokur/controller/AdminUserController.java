package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.service.AdminUserService;

import java.util.UUID;

import java.util.Map;

/**
 * Kontroler administracyjny do zarządzania kontami użytkowników.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Tworzy nowe konto użytkownika przez administratora.
     * Użytkownik tworzony jest z pustym hasłem — musi zostać ustawione przez procedurę resetu hasła.
     *
     * @param request dane nowego użytkownika
     * @return utworzony użytkownik z kodem 201, lub 409 jeśli email jest zajęty, lub 404 jeśli lokal nie istnieje
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = adminUserService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "active", user.isActive(),
                "createdAt", user.getCreatedAt()
            ));
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", message));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try {
            adminUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}

package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.dto.UpdateUserRequest;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.service.AdminUserService;

/** Kontroler administracyjny do zarządzania kontami użytkowników. */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Zwraca stronicowaną listę użytkowników systemu wraz z rolą, lokalem i statusem aktywności.
     *
     * @param page numer strony (0-indexed)
     * @param size rozmiar strony
     * @param search fraza wyszukiwania
     * @return strona użytkowników z kodem 200
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getUsersPage(search, pageable));
    }

    /**
     * Zwraca dane pojedynczego użytkownika po jego identyfikatorze.
     *
     * @param id identyfikator użytkownika
     * @return DTO użytkownika z kodem 200, lub 404 jeśli nie znaleziono
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(adminUserService.getUserById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Tworzy nowe konto użytkownika przez administratora. Użytkownik tworzony jest z pustym hasłem
     * — musi zostać ustawione przez procedurę resetu hasła.
     *
     * @param request dane nowego użytkownika
     * @return utworzony użytkownik z kodem 201, lub 409 jeśli email jest zajęty, lub 404 jeśli
     *     lokal nie istnieje
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = adminUserService.createUser(request);
            UUID apartmentId = user.getUserApartments().isEmpty()
                    ? null
                    : user.getUserApartments().get(0).getApartment().getId();
            UserResponse response = new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.isActive(),
                    user.getCreatedAt(),
                    apartmentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Aktualizuje dane użytkownika: imię, nazwisko, telefon, rolę oraz przypisany lokal.
     *
     * @param id identyfikator użytkownika
     * @param request nowe dane użytkownika
     * @return zaktualizowany użytkownik z kodem 200, lub 404 jeśli użytkownik/lokal nie istnieje
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            User user = adminUserService.updateUser(id, request);
            UUID apartmentId = user.getUserApartments().isEmpty()
                    ? null
                    : user.getUserApartments().get(0).getApartment().getId();
            UserResponse response = new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.isActive(),
                    user.getCreatedAt(),
                    apartmentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Dezaktywuje konto użytkownika ({@code is_active = false}). Historia powiązana z kontem
     * zostaje zachowana.
     *
     * @param id identyfikator użytkownika
     * @return kod 204 po sukcesie, lub 404 jeśli użytkownik nie istnieje
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable UUID id) {
        try {
            adminUserService.deactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Usuwa bezpowrotnie użytkownika o podanym identyfikatorze z systemu.
     *
     * @param id identyfikator użytkownika
     * @return kod 204 po sukcesie, lub 404 jeśli użytkownik nie istnieje
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try {
            adminUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

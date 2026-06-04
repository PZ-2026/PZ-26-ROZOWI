package pl.edu.ur.blokur.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.dto.UserWithTicketsDto;
import pl.edu.ur.blokur.service.UserService;

import java.security.Principal;

/** Kontroler obsługujący żądania odczytu danych użytkowników. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Zwraca listę użytkowników danej roli wraz z ilością ich aktywnych zgłoszeń.
     *
     * @param role rola (np. KONSERWATOR)
     * @return lista użytkowników
     */
    @GetMapping
    public ResponseEntity<List<UserWithTicketsDto>> getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(userService.getUsersWithActiveTicketCountByRole(role));
    }

    /**
     * Pobiera profil aktualnie zalogowanego użytkownika (na podstawie tokena JWT).
     *
     * @param principal obiekt reprezentujący zalogowanego użytkownika (zawiera email)
     * @return DTO z profilem użytkownika
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMe(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }
}

package pl.edu.ur.blokur.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.AuthResponse;
import pl.edu.ur.blokur.dto.LoginRequest;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Kontroler obsługujący uwierzytelnianie użytkowników.
 * Wspiera mechanizm blokady konta po N nieudanych próbach logowania.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        LoginAttemptService loginAttemptService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Loguje użytkownika i zwraca token JWT wraz z rolą.
     *
     * <p>Przed próbą uwierzytelnienia sprawdza czy konto nie jest zablokowane.
     * Po nieudanym logowaniu inkrementuje licznik prób — po 3 błędnych próbach
     * konto zostaje zablokowane na 15 minut. Po udanym logowaniu licznik jest resetowany.</p>
     *
     * @param request dane logowania (email i hasło)
     * @return odpowiedź z tokenem JWT i rolą, 401 przy błędnych danych, lub 423 przy zablokowanym koncie
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getUsername();

        try {
            //try to login, might throw exception for account locke, wrong password, or wrong username
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            //if succed reset failed attemps
            loginAttemptService.resetFailedAttempts(email);

            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .orElse("USER");

            String token = jwtService.generateToken(authentication.getName(), role);

            return ResponseEntity.ok(new AuthResponse(token, role));

        } catch (LockedException e) {
            LocalDateTime lockedUntil = loginAttemptService.getLockedUntil(email);
            String formattedTime = lockedUntil != null
                ? lockedUntil.format(DateTimeFormatter.ofPattern("HH:mm"))
                : "pozniej";

            return ResponseEntity.status(HttpStatus.LOCKED).body(
                Map.of("message", "Konto jest zablokowane. Spróbuj ponownie po " + formattedTime)
            );

        } catch (BadCredentialsException e) {
            loginAttemptService.registerFailedAttempt(email);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Nieprawidłowy email lub hasło")
            );
        } catch (Exception e) {
            System.out.println("UNHANDLED EXCEPTION:" + e.getMessage());
            return null;
        }
    }
}


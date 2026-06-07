package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.AcceptInvitationRequest;
import pl.edu.ur.blokur.dto.AuthResponse;
import pl.edu.ur.blokur.dto.ForgotPasswordRequest;
import pl.edu.ur.blokur.dto.LoginRequest;
import pl.edu.ur.blokur.dto.RefreshTokenRequest;
import pl.edu.ur.blokur.dto.ResetPasswordRequest;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.InvitationService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.PasswordResetService;
import pl.edu.ur.blokur.service.RefreshTokenService;

/**
 * Kontroler obsługujący uwierzytelnianie użytkowników. Wspiera mechanizm blokady konta po N
 * nieudanych próbach logowania.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetService passwordResetService;
    private final InvitationService invitationService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    /**
     * Tworzy kontroler z wymaganymi zależnościami.
     *
     * @param authenticationManager menedżer uwierzytelniania Spring Security
     * @param jwtService serwis JWT
     * @param loginAttemptService serwis blokady kont po nieudanych próbach logowania
     * @param passwordResetService serwis resetu hasła (1 h)
     * @param invitationService serwis zaproszeń nowych użytkowników (72 h)
     * @param refreshTokenService serwis tokenów odświeżania sesji
     * @param userRepository repozytorium użytkowników
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            PasswordResetService passwordResetService,
            InvitationService invitationService,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.passwordResetService = passwordResetService;
        this.invitationService = invitationService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    /**
     * Loguje użytkownika i zwraca token JWT wraz z rolą.
     *
     * <p>Przed próbą uwierzytelnienia sprawdza czy konto nie jest zablokowane. Po nieudanym
     * logowaniu inkrementuje licznik prób — po 3 błędnych próbach konto zostaje zablokowane na 15
     * minut. Po udanym logowaniu licznik jest resetowany.
     *
     * @param request dane logowania (email i hasło)
     * @return odpowiedź z tokenem JWT i rolą, 401 przy błędnych danych, lub 423 przy zablokowanym
     *     koncie
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getUsername();

        try {
            // try to login, might throw exception for account locke, wrong password, or wrong
            // username
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(email, request.getPassword()));

            // if succed reset failed attemps
            loginAttemptService.resetFailedAttempts(email);

            String role =
                    authentication.getAuthorities().stream()
                            .findFirst()
                            .map(GrantedAuthority::getAuthority)
                            .map(a -> a.replace("ROLE_", ""))
                            .orElse("USER");

            String token = jwtService.generateToken(authentication.getName(), role);

            User user =
                    userRepository
                            .findByEmail(authentication.getName())
                            .orElseThrow(
                                    () -> new IllegalStateException("Użytkownik nie istnieje"));
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

            return ResponseEntity.ok(new AuthResponse(token, refreshToken, role));

        } catch (LockedException e) {
            LocalDateTime lockedUntil = loginAttemptService.getLockedUntil(email);
            String formattedTime =
                    lockedUntil != null
                            ? lockedUntil.format(DateTimeFormatter.ofPattern("HH:mm"))
                            : "pozniej";

            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(
                            Map.of(
                                    "message",
                                    "Konto jest zablokowane. Spróbuj ponownie po "
                                            + formattedTime));

        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Konto jest nieaktywne. Skontaktuj się z administratorem."));
        } catch (BadCredentialsException e) {
            loginAttemptService.registerFailedAttempt(email);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Nieprawidłowy email lub hasło"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Nieoczekiwany błąd serwera"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenService.TokenPair pair =
                    refreshTokenService.exchange(request.getRefreshToken());
            return ResponseEntity.ok(
                    new AuthResponse(pair.accessToken(), pair.refreshToken(), pair.role()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Jeśli podany adres e-mail istnieje w systemie, wysłaliśmy link do"
                                + " resetowania hasła."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(
                    Map.of("message", "Hasło zostało zmienione. Możesz się teraz zalogować."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Przyjmuje zaproszenie — ustawia hasło dla nowo utworzonego konta.
     *
     * <p>Token jest ważny przez 72 h od chwili wysyłki. Wygasły token skutkuje odpowiedzią 410
     * (Gone), nieistniejący — 400 (Bad Request).
     *
     * @param request token z linka aktywacyjnego oraz wybrane hasło
     * @return 200 po pomyślnym ustawieniu hasła, 400 przy nieprawidłowym tokenie
     */
    @PostMapping("/accept-invitation")
    public ResponseEntity<?> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        try {
            invitationService.acceptInvitation(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(
                    Map.of("message", "Hasło zostało ustawione. Możesz się teraz zalogować."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

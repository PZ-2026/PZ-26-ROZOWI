package pl.edu.ur.blokur.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.LoginAttemptService;

/**
 * Implementacja {@link UserDetailsService} pobierająca dane użytkownika z bazy PostgreSQL.
 * Zastępuje wcześniejszy {@code InMemoryUserDetailsManager} z hardkodowanymi użytkownikami.
 *
 * <p>Logowanie odbywa się za pomocą adresu email jako nazwy użytkownika. Weryfikacja hasła (BCrypt)
 * jest wykonywana automatycznie przez Spring Security.
 *
 * <p>Blokada konta po nieudanych próbach logowania jest obsługiwana przez {@link
 * LoginAttemptService} (in-memory), a nie przez pola w encji.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public CustomUserDetailsService(
            UserRepository userRepository, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Wczytuje dane użytkownika na podstawie adresu email. Sprawdza czy konto jest aktywne oraz czy
     * nie jest zablokowane.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Nie znaleziono użytkownika o emailu: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .disabled(!user.isActive())
                .accountLocked(loginAttemptService.isAccountLocked(user.getEmail()))
                .build();
    }
}

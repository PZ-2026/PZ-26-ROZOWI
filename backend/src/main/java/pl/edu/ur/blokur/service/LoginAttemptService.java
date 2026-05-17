package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Serwis zapobiegający atakom brute-force. Śledzi nieudane próby logowania w pamięci podręcznej
 * (RAM).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Rejestruje nieudane logowanie i ewentualnie blokuje konto po przekroczeniu limitu prób.
     *
     * @param email adres e-mail, którym próbowano się zalogować
     */
    public void registerFailedAttempt(String email) {
        var info = attemptsCache.getOrDefault(email, new AttemptInfo());

        info.incrementAttempts();

        if (info.getAttempts() >= MAX_ATTEMPTS) {
            info.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        attemptsCache.put(email, info);
    }

    /**
     * Resetuje licznik nieudanych prób po udanym logowaniu.
     *
     * @param email adres e-mail użytkownika, który się zalogował
     */
    public void resetFailedAttempts(String email) {
        attemptsCache.remove(email);
    }

    /**
     * Sprawdza, czy konto jest obecnie zablokowane (blokada automatycznie wygasa po upływie
     * ustalonego czasu).
     *
     * @param email adres e-mail konta
     * @return {@code true} jeśli konto jest zablokowane
     */
    public boolean isAccountLocked(String email) {
        var info = attemptsCache.get(email);

        if (info == null || info.getLockedUntil() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(info.getLockedUntil())) {
            resetFailedAttempts(email);
            return false;
        }

        return true;
    }

    /**
     * Zwraca czas, do którego konto pozostaje zablokowane.
     *
     * @param email adres e-mail konta
     * @return moment wygaśnięcia blokady lub {@code null}, jeśli konto nie jest zablokowane
     */
    public LocalDateTime getLockedUntil(String email) {
        var info = attemptsCache.get(email);
        return info != null ? info.getLockedUntil() : null;
    }

    private static class AttemptInfo {
        private int attempts = 0;
        private LocalDateTime lockedUntil = null;

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLockedUntil() {
            return lockedUntil;
        }

        public void setLockedUntil(LocalDateTime lockedUntil) {
            this.lockedUntil = lockedUntil;
        }
    }
}

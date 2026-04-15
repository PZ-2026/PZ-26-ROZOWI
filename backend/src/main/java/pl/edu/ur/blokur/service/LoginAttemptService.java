package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serwis zapobiegający atakom brute-force.
 * Śledzi nieudane próby logowania w pamięci podręcznej (RAM).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Rejestruje nieudane logowanie i ewentualnie blokuje konto.
     */
    public void registerFailedAttempt(String email) {
        AttemptInfo info = attemptsCache.getOrDefault(email, new AttemptInfo());

        info.incrementAttempts();

        if (info.getAttempts() >= MAX_ATTEMPTS) {
            info.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        attemptsCache.put(email, info);
    }

    /**
     * Resetuje licznik po udanym logowaniu.
     */
    public void resetFailedAttempts(String email) {
        attemptsCache.remove(email);
    }

    /**
     * Sprawdza, czy konto jest obecnie zablokowane.
     */
    public boolean isAccountLocked(String email) {
        AttemptInfo info = attemptsCache.get(email);

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
     * Zwraca czas, do którego konto jest zablokowane.
     */
    public LocalDateTime getLockedUntil(String email) {
        AttemptInfo info = attemptsCache.get(email);
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

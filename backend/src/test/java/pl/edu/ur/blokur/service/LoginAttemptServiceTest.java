package pl.edu.ur.blokur.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe dla {@link LoginAttemptService}.
 * Weryfikują mechanizm blokowania konta po błędnych próbach logowania.
 */
@DisplayName("LoginAttemptService — blokowanie konta")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    private static final String EMAIL = "test@blokur.pl";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    // -------------------------------------------------------
    // Stan początkowy
    // -------------------------------------------------------

    @Test
    @DisplayName("Nowe konto nie jest zablokowane")
    void shouldNotBeLockedInitially() {
        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("getLockedUntil zwraca null dla niezablokowanego konta")
    void shouldReturnNullLockedUntilForFreshAccount() {
        assertThat(loginAttemptService.getLockedUntil(EMAIL)).isNull();
    }

    // -------------------------------------------------------
    // Kolejne nieudane próby — przed limitem
    // -------------------------------------------------------

    @Test
    @DisplayName("Po 1 błędnej próbie konto nie jest zablokowane")
    void shouldNotLockAfterOneFailedAttempt() {
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("Po 2 błędnych próbach konto nie jest zablokowane")
    void shouldNotLockAfterTwoFailedAttempts() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isFalse();
    }

    // -------------------------------------------------------
    // Blokada po 3 próbach
    // -------------------------------------------------------

    @Test
    @DisplayName("Po 3 błędnych próbach konto zostaje zablokowane")
    void shouldLockAccountAfterThreeFailedAttempts() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("Po 3 błędnych próbach czas blokady jest ustawiony na ~15 minut")
    void shouldSetLockDurationToFifteenMinutes() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        LocalDateTime lockedUntil = loginAttemptService.getLockedUntil(EMAIL);

        assertThat(lockedUntil).isNotNull();
        assertThat(lockedUntil).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThat(lockedUntil).isBefore(LocalDateTime.now().plusMinutes(16));
    }

    @Test
    @DisplayName("Po 4 i więcej błędnych próbach konto nadal pozostaje zablokowane")
    void shouldStayLockedAfterMoreThanThreeAttempts() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isTrue();
    }

    // -------------------------------------------------------
    // Reset po udanym logowaniu
    // -------------------------------------------------------

    @Test
    @DisplayName("Reset przywraca stan — konto nie jest zablokowane po resecie")
    void shouldUnlockAfterReset() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        loginAttemptService.resetFailedAttempts(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("Po resecie getLockedUntil zwraca null")
    void shouldReturnNullLockedUntilAfterReset() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        loginAttemptService.resetFailedAttempts(EMAIL);

        assertThat(loginAttemptService.getLockedUntil(EMAIL)).isNull();
    }

    @Test
    @DisplayName("Po resecie ponowne 3 próby znowu blokują konto")
    void shouldLockAgainAfterResetAndThreeMoreAttempts() {
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.resetFailedAttempts(EMAIL);

        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isTrue();
    }

    // -------------------------------------------------------
    // Izolacja — różne emaile nie wpływają na siebie
    // -------------------------------------------------------

    @Test
    @DisplayName("Blokada jednego konta nie wpływa na inne konto")
    void shouldIsolateAttemptsBetweenDifferentEmails() {
        String otherEmail = "inny@blokur.pl";

        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(otherEmail)).isFalse();
    }

    @Test
    @DisplayName("Reset jednego konta nie resetuje innego konta")
    void shouldIsolateResetBetweenDifferentEmails() {
        String otherEmail = "inny@blokur.pl";

        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);
        loginAttemptService.registerFailedAttempt(EMAIL);

        loginAttemptService.registerFailedAttempt(otherEmail);
        loginAttemptService.registerFailedAttempt(otherEmail);
        loginAttemptService.registerFailedAttempt(otherEmail);

        loginAttemptService.resetFailedAttempts(EMAIL);

        assertThat(loginAttemptService.isAccountLocked(EMAIL)).isFalse();
        assertThat(loginAttemptService.isAccountLocked(otherEmail)).isTrue();
    }

    // -------------------------------------------------------
    // Wygaśnięcie blokady (przez reset — bez oczekiwania 15 min)
    // -------------------------------------------------------

    @Test
    @DisplayName("Reset nieistniejącego konta nie rzuca wyjątku")
    void shouldNotThrowWhenResettingNonExistentAccount() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> loginAttemptService.resetFailedAttempts("nieistniejacy@blokur.pl")
        );
    }
}

package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO żądania ustawienia nowego hasła przy użyciu tokenu resetującego. */
public class ResetPasswordRequest {

    @NotBlank private String token;

    @NotBlank
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Hasło musi zawierać co najmniej jedną wielką literę i jedną cyfrę")
    private String newPassword;

    /**
     * Zwraca wartość tokenu resetującego hasło.
     *
     * @return wartość tokenu
     */
    public String getToken() {
        return token;
    }

    /**
     * Ustawia wartość tokenu resetującego hasło.
     *
     * @param token wartość tokenu
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Zwraca nowe hasło podane przez użytkownika.
     *
     * @return nowe hasło (przed hashowaniem)
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Ustawia nowe hasło podane przez użytkownika.
     *
     * @param newPassword nowe hasło (przed hashowaniem)
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

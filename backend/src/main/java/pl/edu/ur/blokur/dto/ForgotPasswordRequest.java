package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO żądania wygenerowania linku do resetu hasła (użytkownik zapomniał hasła). */
public class ForgotPasswordRequest {

    @NotBlank @Email private String email;

    /**
     * Zwraca adres e-mail, na który ma zostać wysłany link resetujący.
     *
     * @return adres e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Ustawia adres e-mail, na który ma zostać wysłany link resetujący.
     *
     * @param email adres e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }
}

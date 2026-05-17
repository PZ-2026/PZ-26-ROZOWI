package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** DTO żądania wygenerowania linku do resetu hasła (użytkownik zapomniał hasła). */
@Data
public class ForgotPasswordRequest {

    @NotBlank @Email private String email;
}

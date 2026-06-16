package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO żądania przyjęcia zaproszenia: 6-cyfrowy kod + nowe hasło. */
@Data
public class AcceptInvitationRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "Kod musi składać się z 6 cyfr")
    private String code;

    @NotBlank
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Hasło musi zawierać co najmniej jedną wielką literę i jedną cyfrę")
    private String newPassword;
}

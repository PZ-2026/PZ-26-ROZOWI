package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO żądania przyjęcia zaproszenia i ustawienia hasła do nowego konta. */
@Data
public class AcceptInvitationRequest {

    @NotBlank private String token;

    @NotBlank
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Hasło musi zawierać co najmniej jedną wielką literę i jedną cyfrę")
    private String newPassword;
}

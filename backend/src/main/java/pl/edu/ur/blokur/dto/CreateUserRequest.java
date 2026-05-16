package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

/** DTO wejściowe z danymi nowego użytkownika tworzonego przez zarządcę. */
@Data
public class CreateUserRequest {

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    @NotBlank @Email private String email;

    @NotBlank private String role;

    @NotNull private UUID apartmentId;
}

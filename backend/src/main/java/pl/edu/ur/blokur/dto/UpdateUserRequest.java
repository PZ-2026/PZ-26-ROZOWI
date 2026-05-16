package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

/** DTO wejściowe z danymi do aktualizacji istniejącego użytkownika przez zarządcę. */
@Data
public class UpdateUserRequest {

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    private String phone;

    @NotBlank private String role;

    private UUID apartmentId;
}

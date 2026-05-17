package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/** DTO reprezentujące żądanie utworzenia nowej uchwały. */
@Data
public class CreateResolutionRequest {

    @NotBlank(message = "Tytuł jest wymagany")
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    private String description;

    @NotNull(message = "Data zakończenia jest wymagana")
    @Future(message = "Data zakończenia musi być w przyszłości")
    private LocalDateTime endDate;

    @NotNull(message = "Opcje są wymagane")
    @Size(min = 2, max = 10, message = "Liczba opcji musi wynosić od 2 do 10")
    private List<@NotBlank(message = "Opcja nie może być pusta") String> options;

    @NotNull(message = "ID budynku jest wymagane")
    private UUID targetBuildingId;
}

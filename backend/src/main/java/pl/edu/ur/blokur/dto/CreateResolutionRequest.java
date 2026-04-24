package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** DTO reprezentujące żądanie utworzenia nowej uchwały. */
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public UUID getTargetBuildingId() {
        return targetBuildingId;
    }

    public void setTargetBuildingId(UUID targetBuildingId) {
        this.targetBuildingId = targetBuildingId;
    }
}

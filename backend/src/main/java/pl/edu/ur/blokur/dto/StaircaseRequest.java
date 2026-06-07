package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO z danymi klatki schodowej przesyłanymi przez klienta (tworzenie i aktualizacja). */
@Data
public class StaircaseRequest {

    @NotBlank(message = "Etykieta klatki nie może być pusta")
    @Size(max = 50, message = "Etykieta klatki nie może przekraczać 50 znaków")
    private String label;
}

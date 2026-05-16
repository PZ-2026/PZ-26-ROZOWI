package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO wejściowe dla operacji tworzenia/aktualizacji kategorii zgłoszenia. */
@Data
public class CategoryRequest {

    @NotBlank(message = "Nazwa kategorii nie może być pusta")
    @Size(max = 100, message = "Nazwa kategorii nie może przekraczać 100 znaków")
    private String name;
}

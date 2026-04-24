package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO wejściowe dla operacji tworzenia/aktualizacji kategorii zgłoszenia. */
public class CategoryRequest {

    @NotBlank(message = "Nazwa kategorii nie może być pusta")
    @Size(max = 100, message = "Nazwa kategorii nie może przekraczać 100 znaków")
    private String name;

    /**
     * Zwraca nazwę kategorii.
     *
     * @return nazwa kategorii
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę kategorii.
     *
     * @param name nazwa kategorii
     */
    public void setName(String name) {
        this.name = name;
    }
}

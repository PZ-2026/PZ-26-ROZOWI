package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO wyjściowe reprezentujące kategorię zgłoszenia zwracane klientom API. */
@Data
public class CategoryResponse {

    private UUID id;
    private String name;

    /**
     * Tworzy DTO kategorii.
     *
     * @param id identyfikator kategorii
     * @param name nazwa kategorii
     */
    public CategoryResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}

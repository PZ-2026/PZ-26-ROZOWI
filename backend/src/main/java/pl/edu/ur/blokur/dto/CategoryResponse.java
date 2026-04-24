package pl.edu.ur.blokur.dto;

import java.util.UUID;

/** DTO wyjściowe reprezentujące kategorię zgłoszenia zwracane klientom API. */
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

    /**
     * Zwraca unikalny identyfikator kategorii.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator kategorii.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

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

package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO wyjściowe reprezentujące kategorię zgłoszenia zwracane klientom API. */
@Data
public class CategoryResponse {

    private UUID id;
    private String name;
    private Integer slaHours;

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
     * Tworzy DTO kategorii z informacją o SLA.
     *
     * @param id identyfikator kategorii
     * @param name nazwa kategorii
     * @param slaHours godziny SLA
     */
    public CategoryResponse(UUID id, String name, Integer slaHours) {
        this.id = id;
        this.name = name;
        this.slaHours = slaHours;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
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

package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** DTO dla listy uchwał. */
@Data
public class ResolutionDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime endDate;
    private UUID buildingId;
    private String authorName;

    /**
     * Tworzy DTO uchwały na potrzeby listy uchwał.
     *
     * @param id identyfikator uchwały
     * @param title tytuł uchwały
     * @param description opis uchwały
     * @param endDate data zakończenia głosowania
     * @param buildingId identyfikator budynku powiązanego z uchwałą
     * @param authorName imię i nazwisko autora uchwały
     */
    public ResolutionDto(
            UUID id,
            String title,
            String description,
            LocalDateTime endDate,
            UUID buildingId,
            String authorName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.endDate = endDate;
        this.buildingId = buildingId;
        this.authorName = authorName;
    }
}

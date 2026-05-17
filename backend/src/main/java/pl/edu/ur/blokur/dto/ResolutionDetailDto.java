package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/** DTO dla szczegółów uchwały z opcjonalnymi wynikami. */
@Data
public class ResolutionDetailDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime endDate;
    private UUID buildingId;
    private String authorName;
    private List<ResolutionOptionDto> options;
    private List<ResolutionOptionResultDto> results;

    public ResolutionDetailDto(
            UUID id,
            String title,
            String description,
            LocalDateTime endDate,
            UUID buildingId,
            String authorName,
            List<ResolutionOptionDto> options,
            List<ResolutionOptionResultDto> results) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.endDate = endDate;
        this.buildingId = buildingId;
        this.authorName = authorName;
        this.options = options;
        this.results = results;
    }
}

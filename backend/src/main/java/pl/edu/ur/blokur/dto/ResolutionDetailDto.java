package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** DTO dla szczegółów uchwały z opcjonalnymi wynikami. */
public class ResolutionDetailDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime endDate;
    private UUID buildingId;
    private String authorName;
    private List<ResolutionOptionDto> options;
    private List<ResolutionOptionResultDto> results;

    public ResolutionDetailDto() {}

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public UUID getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(UUID buildingId) {
        this.buildingId = buildingId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public List<ResolutionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<ResolutionOptionDto> options) {
        this.options = options;
    }

    public List<ResolutionOptionResultDto> getResults() {
        return results;
    }

    public void setResults(List<ResolutionOptionResultDto> results) {
        this.results = results;
    }
}

package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO dla listy uchwał. */
public class ResolutionDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime endDate;
    private UUID buildingId;
    private String authorName;

    public ResolutionDto() {}

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
}

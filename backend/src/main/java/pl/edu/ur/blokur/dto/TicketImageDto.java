package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import pl.edu.ur.blokur.models.TicketImageType;

/** DTO reprezentujące zdjęcie przypisane do zgłoszenia. */
public class TicketImageDto {

    private UUID id;
    private UUID ticketId;
    private UUID uploaderId;
    private TicketImageType imageType;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private String url;

    public TicketImageDto() {}

    public TicketImageDto(
            UUID id,
            UUID ticketId,
            UUID uploaderId,
            TicketImageType imageType,
            String originalFilename,
            LocalDateTime uploadedAt,
            String url) {
        this.id = id;
        this.ticketId = ticketId;
        this.uploaderId = uploaderId;
        this.imageType = imageType;
        this.originalFilename = originalFilename;
        this.uploadedAt = uploadedAt;
        this.url = url;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(UUID uploaderId) {
        this.uploaderId = uploaderId;
    }

    public TicketImageType getImageType() {
        return imageType;
    }

    public void setImageType(TicketImageType imageType) {
        this.imageType = imageType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

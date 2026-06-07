package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.TicketImageType;

/** DTO reprezentujące zdjęcie przypisane do zgłoszenia. */
@Data
public class TicketImageDto {

    private UUID id;
    private UUID ticketId;
    private UUID uploaderId;
    private TicketImageType imageType;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private String url;

    /**
     * Tworzy DTO zdjęcia przypisanego do zgłoszenia.
     *
     * @param id identyfikator zdjęcia
     * @param ticketId identyfikator zgłoszenia
     * @param uploaderId identyfikator użytkownika, który wgrał zdjęcie
     * @param imageType typ zdjęcia (BEFORE/AFTER)
     * @param originalFilename oryginalna nazwa pliku
     * @param uploadedAt data i czas wgrania
     * @param url URL dostępu do zdjęcia
     */
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
}

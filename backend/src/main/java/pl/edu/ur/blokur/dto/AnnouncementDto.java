package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * Obiekt transferu danych dla ogłoszeń. Służy do zapobiegania wyciekom detali bazy danych i
 * zjawisku niekontrolowanego leniwego czytania podczas serializacji.
 */
@Data
public class AnnouncementDto {

    private UUID id;
    private String type;
    private String title;
    private String content;
    private String authorName;
    private String targetType;
    private String attachmentUrl;
    private LocalDateTime plannedDate;
    private LocalDateTime createdAt;

    /**
     * Konstruktor parametryczny tworzący kompletny obiekt DTO.
     *
     * @param id unikalny identyfikator ogłoszenia
     * @param type typ ogłoszenia (np. OGLOSZENIE, KOMUNIKAT)
     * @param title tytuł ogłoszenia
     * @param content treść ogłoszenia
     * @param authorName imię i nazwisko autora
     * @param targetType typ zasięgu ogłoszenia
     * @param attachmentUrl URL do załącznika PDF
     * @param plannedDate data planowanego opublikowania
     * @param createdAt data i czas utworzenia rekordu
     */
    public AnnouncementDto(
            UUID id,
            String type,
            String title,
            String content,
            String authorName,
            String targetType,
            String attachmentUrl,
            LocalDateTime plannedDate,
            LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.targetType = targetType;
        this.attachmentUrl = attachmentUrl;
        this.plannedDate = plannedDate;
        this.createdAt = createdAt;
    }
}

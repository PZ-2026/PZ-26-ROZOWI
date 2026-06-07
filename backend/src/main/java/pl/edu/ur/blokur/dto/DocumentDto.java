package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

/** DTO reprezentujące dokument. */
@Data
public class DocumentDto {
    private UUID id;
    private String type;
    private String title;
    private LocalDateTime createdAt;
    private String downloadUrl;

    /**
     * Służy do tworzenia dokumentu
     *
     * @param id identyfikator dokumentu
     * @param type typ dokumentu
     * @param title tytuł dokumentu
     * @param createdAt data utworzenia
     * @param downloadUrl link do pobrania dokumentu
     */
    public DocumentDto(
            UUID id, String type, String title, LocalDateTime createdAt, String downloadUrl) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.createdAt = createdAt;
        this.downloadUrl = downloadUrl;
    }
}

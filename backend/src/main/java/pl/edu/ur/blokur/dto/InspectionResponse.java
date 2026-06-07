package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.ScopeType;

/** DTO z danymi przeglądu technicznego zwracanymi przez API. */
@Data
public class InspectionResponse {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime scheduledAt;
    private ScopeType scopeType;
    private UUID scopeId;
    private String createdByName;
    private LocalDateTime createdAt;

    /**
     * Konstruktor wszystkich pól.
     *
     * @param id identyfikator przeglądu
     * @param title tytuł przeglądu
     * @param description opis przeglądu (może być {@code null})
     * @param scheduledAt planowana data i godzina przeglądu
     * @param scopeType typ zasięgu przeglądu
     * @param scopeId UUID encji zasięgu
     * @param createdByName imię i nazwisko twórcy przeglądu
     * @param createdAt data i czas utworzenia rekordu
     */
    public InspectionResponse(
            UUID id,
            String title,
            String description,
            LocalDateTime scheduledAt,
            ScopeType scopeType,
            UUID scopeId,
            String createdByName,
            LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }
}

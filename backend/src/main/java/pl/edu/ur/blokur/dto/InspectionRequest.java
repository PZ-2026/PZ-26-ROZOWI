package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.ScopeType;

/** DTO z danymi przeglądu technicznego przesyłanymi przez klienta (tworzenie i aktualizacja). */
@Data
public class InspectionRequest {

    @NotBlank(message = "Tytuł przeglądu nie może być pusty")
    @Size(max = 255, message = "Tytuł nie może przekraczać 255 znaków")
    private String title;

    private String description;

    @NotNull(message = "Data przeglądu jest wymagana")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Typ zasięgu jest wymagany")
    private ScopeType scopeType;

    @NotNull(message = "Identyfikator zakresu jest wymagany")
    private UUID scopeId;

    /**
     * Konstruktor wszystkich pól — używany w testach jednostkowych.
     *
     * @param title tytuł przeglądu
     * @param description opis przeglądu (opcjonalny)
     * @param scheduledAt planowana data i godzina
     * @param scopeType typ zasięgu przeglądu
     * @param scopeId UUID encji zasięgu
     */
    public InspectionRequest(
            String title,
            String description,
            LocalDateTime scheduledAt,
            ScopeType scopeType,
            UUID scopeId) {
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
    }
}

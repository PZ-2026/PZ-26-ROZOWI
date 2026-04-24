package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import pl.edu.ur.blokur.models.ScopeType;

/** DTO z danymi przeglądu technicznego przesyłanymi przez klienta (tworzenie i aktualizacja). */
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

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public InspectionRequest() {}

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

    /**
     * Zwraca tytuł przeglądu.
     *
     * @return tytuł przeglądu
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł przeglądu.
     *
     * @param title tytuł przeglądu
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca opis przeglądu.
     *
     * @return opis lub {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ustawia opis przeglądu.
     *
     * @param description opis przeglądu
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Zwraca planowaną datę i godzinę przeglądu.
     *
     * @return data i godzina przeglądu
     */
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    /**
     * Ustawia planowaną datę i godzinę przeglądu.
     *
     * @param scheduledAt data i godzina przeglądu
     */
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    /**
     * Zwraca typ zasięgu przeglądu.
     *
     * @return zasięg (NIERUCHOMOSC, BUDYNEK lub KLATKA)
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * Ustawia typ zasięgu przeglądu.
     *
     * @param scopeType typ zasięgu
     */
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    /**
     * Zwraca identyfikator encji zasięgu.
     *
     * @return UUID nieruchomości, budynku lub klatki schodowej
     */
    public UUID getScopeId() {
        return scopeId;
    }

    /**
     * Ustawia identyfikator encji zasięgu.
     *
     * @param scopeId UUID encji zasięgu
     */
    public void setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
    }
}

package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import pl.edu.ur.blokur.models.ScopeType;

/** DTO z danymi przeglądu technicznego zwracanymi przez API. */
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

    /**
     * Zwraca identyfikator przeglądu.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator przeglądu.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
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
     * @return opis lub {@code null} jeśli nie podano
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

    /**
     * Zwraca imię i nazwisko użytkownika, który utworzył przegląd.
     *
     * @return imię i nazwisko twórcy
     */
    public String getCreatedByName() {
        return createdByName;
    }

    /**
     * Ustawia imię i nazwisko twórcy przeglądu.
     *
     * @param createdByName imię i nazwisko
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    /**
     * Zwraca datę i czas utworzenia rekordu.
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia rekordu.
     *
     * @param createdAt data i czas utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import pl.edu.ur.blokur.models.AnnouncementTargetType;

/**
 * DTO służące do tworzenia i edycji ogłoszenia przez zarządcę. Zawiera walidację pól wymaganych.
 */
public class AnnouncementRequest {

    @NotBlank(message = "Tytuł ogłoszenia nie może być pusty")
    private String title;

    @NotBlank(message = "Treść ogłoszenia nie może być pusta")
    private String content;

    @NotNull(message = "Typ zasięgu musi zostać określony")
    private AnnouncementTargetType targetType;

    private UUID targetId;

    private LocalDateTime plannedDate;

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public AnnouncementRequest() {}

    /**
     * Zwraca tytuł ogłoszenia.
     *
     * @return tytuł ogłoszenia
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ustawia tytuł ogłoszenia.
     *
     * @param title tytuł ogłoszenia
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Zwraca treść ogłoszenia (plain HTML).
     *
     * @return treść ogłoszenia
     */
    public String getContent() {
        return content;
    }

    /**
     * Ustawia treść ogłoszenia.
     *
     * @param content treść ogłoszenia
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Zwraca typ zasięgu ogłoszenia.
     *
     * @return typ zasięgu (WSZYSCY, NIERUCHOMOSC, BUDYNEK, KLATKA)
     */
    public AnnouncementTargetType getTargetType() {
        return targetType;
    }

    /**
     * Ustawia typ zasięgu ogłoszenia.
     *
     * @param targetType typ zasięgu
     */
    public void setTargetType(AnnouncementTargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * Zwraca identyfikator celu (budynku, klatki lub lokalu).
     *
     * @return UUID celu lub null dla WSZYSCY
     */
    public UUID getTargetId() {
        return targetId;
    }

    /**
     * Ustawia identyfikator celu.
     *
     * @param targetId UUID celu
     */
    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    /**
     * Zwraca planowaną datę publikacji.
     *
     * @return data planowanej publikacji lub null
     */
    public LocalDateTime getPlannedDate() {
        return plannedDate;
    }

    /**
     * Ustawia planowaną datę publikacji.
     *
     * @param plannedDate data planowanej publikacji
     */
    public void setPlannedDate(LocalDateTime plannedDate) {
        this.plannedDate = plannedDate;
    }
}

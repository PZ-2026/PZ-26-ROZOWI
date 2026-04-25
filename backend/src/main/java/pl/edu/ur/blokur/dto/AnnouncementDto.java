package pl.edu.ur.blokur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Obiekt transferu danych dla ogłoszeń. Służy do zapobiegania wyciekom detali bazy danych i
 * zjawisku niekontrolowanego leniwego czytania podczas serializacji.
 */
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
     * Liczba odbiorców powiadomienia PUSH wysłanego przy tworzeniu ogłoszenia. Wypełniana tylko w
     * odpowiedzi na żądanie tworzenia (POST); przy pobieraniu listy ogłoszeń wynosi {@code 0}.
     */
    private long recipientCount;

    /** Konstruktor bezargumentowy wymagany przez mechanizmy serializacji. */
    public AnnouncementDto() {}

    /**
     * Konstruktor parametryczny tworzący kompletny obiekt DTO (bez liczby odbiorców).
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

    /**
     * Konstruktor parametryczny tworzący kompletny obiekt DTO z liczbą odbiorców.
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
     * @param recipientCount liczba odbiorców powiadomienia PUSH
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
            LocalDateTime createdAt,
            long recipientCount) {
        this(id, type, title, content, authorName, targetType, attachmentUrl, plannedDate, createdAt);
        this.recipientCount = recipientCount;
    }

    /**
     * Zwraca unikalny identyfikator ogłoszenia.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator ogłoszenia.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca typ ogłoszenia.
     *
     * @return typ ogłoszenia
     */
    public String getType() {
        return type;
    }

    /**
     * Ustawia typ ogłoszenia.
     *
     * @param type typ ogłoszenia
     */
    public void setType(String type) {
        this.type = type;
    }

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
     * Zwraca treść ogłoszenia.
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
     * Zwraca imię i nazwisko autora ogłoszenia.
     *
     * @return imię i nazwisko autora
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Ustawia imię i nazwisko autora ogłoszenia.
     *
     * @param authorName imię i nazwisko autora
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Zwraca typ zasięgu ogłoszenia.
     *
     * @return typ zasięgu
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Ustawia typ zasięgu ogłoszenia.
     *
     * @param targetType typ zasięgu
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * Zwraca URL do załącznika PDF.
     *
     * @return URL do załącznika lub null
     */
    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    /**
     * Ustawia URL do załącznika PDF.
     *
     * @param attachmentUrl URL do załącznika
     */
    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    /**
     * Zwraca planowaną datę publikacji ogłoszenia.
     *
     * @return data planowanej publikacji lub null jeśli nie określono
     */
    public LocalDateTime getPlannedDate() {
        return plannedDate;
    }

    /**
     * Ustawia planowaną datę publikacji ogłoszenia.
     *
     * @param plannedDate data planowanej publikacji
     */
    public void setPlannedDate(LocalDateTime plannedDate) {
        this.plannedDate = plannedDate;
    }

    /**
     * Zwraca datę i czas utworzenia ogłoszenia.
     *
     * @return data i czas utworzenia rekordu
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę i czas utworzenia ogłoszenia.
     *
     * @param createdAt data i czas utworzenia rekordu
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Zwraca liczbę odbiorców powiadomienia PUSH dla tego ogłoszenia.
     *
     * <p>Wartość jest wypełniana tylko w odpowiedzi na żądanie tworzenia ogłoszenia (POST). W
     * pozostałych przypadkach wynosi {@code 0}.
     *
     * @return liczba odbiorców powiadomienia PUSH
     */
    public long getRecipientCount() {
        return recipientCount;
    }

    /**
     * Ustawia liczbę odbiorców powiadomienia PUSH.
     *
     * @param recipientCount liczba odbiorców
     */
    public void setRecipientCount(long recipientCount) {
        this.recipientCount = recipientCount;
    }
}

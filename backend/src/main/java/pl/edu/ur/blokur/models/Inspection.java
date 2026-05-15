package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Encja reprezentująca przegląd techniczny zaplanowany dla wybranego zakresu nieruchomości. Zasięg
 * przeglądu określany jest przez {@link ScopeType} i odpowiadający mu {@code scopeId}.
 */
@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    @Column(name = "scope_id", nullable = false)
    private UUID scopeId;

    @Column(name = "notified_24h", nullable = false)
    @ColumnDefault("false")
    private boolean notified24h = false;

    @Column(name = "notified_7d", nullable = false)
    @ColumnDefault("false")
    private boolean notified7d = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * Zwraca unikalny identyfikator przeglądu.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator przeglądu.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca tytuł przeglądu.
     *
     * @return tytuł przeglądu (np. "Przegląd gazowy")
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
     * Zwraca identyfikator encji odpowiadającej zasięgowi.
     *
     * @return UUID nieruchomości, budynku lub klatki schodowej
     */
    public UUID getScopeId() {
        return scopeId;
    }

    /**
     * Ustawia identyfikator encji odpowiadającej zasięgowi.
     *
     * @param scopeId UUID encji zasięgu
     */
    public void setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * Zwraca flagę informującą, czy powiadomienie 24-godzinne zostało już wysłane.
     *
     * @return {@code true} jeśli powiadomienie 24h zostało wysłane
     */
    public boolean isNotified24h() {
        return notified24h;
    }

    /**
     * Ustawia flagę powiadomienia 24-godzinnego.
     *
     * @param notified24h {@code true} po wysłaniu powiadomienia 24h
     */
    public void setNotified24h(boolean notified24h) {
        this.notified24h = notified24h;
    }

    /**
     * Zwraca flagę informującą, czy powiadomienie 7-dniowe zostało już wysłane.
     *
     * @return {@code true} jeśli powiadomienie 7d zostało wysłane
     */
    public boolean isNotified7d() {
        return notified7d;
    }

    /**
     * Ustawia flagę powiadomienia 7-dniowego.
     *
     * @param notified7d {@code true} po wysłaniu powiadomienia 7d
     */
    public void setNotified7d(boolean notified7d) {
        this.notified7d = notified7d;
    }

    /**
     * Zwraca użytkownika, który utworzył przegląd.
     *
     * @return encja użytkownika-twórcy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * Ustawia użytkownika, który utworzył przegląd.
     *
     * @param createdBy encja użytkownika-twórcy
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

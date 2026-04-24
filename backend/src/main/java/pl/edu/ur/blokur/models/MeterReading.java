package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Encja reprezentująca odczyt licznika dla danego lokalu. Odczyt jest powiązany z konkretnym
 * licznikiem ({@link Meter}) — a przez niego z lokalem.
 */
@Entity
@Table(name = "meter_readings")
@EntityListeners(AuditingEntityListener.class)
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "value", nullable = false, precision = 12, scale = 4)
    private BigDecimal value;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "recorded_by", updatable = false, length = 255)
    private String recordedBy;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Zwraca unikalny identyfikator odczytu.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator odczytu.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca lokal, którego dotyczy odczyt.
     *
     * @return encja lokalu
     */
    public Apartment getApartment() {
        return apartment;
    }

    /**
     * Ustawia lokal, którego dotyczy odczyt.
     *
     * @param apartment encja lokalu
     */
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    /**
     * Zwraca licznik, z którego pochodzi odczyt.
     *
     * @return encja licznika
     */
    public Meter getMeter() {
        return meter;
    }

    /**
     * Ustawia licznik, z którego pochodzi odczyt.
     *
     * @param meter encja licznika
     */
    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    /**
     * Zwraca wartość odczytu.
     *
     * @return wartość odczytu
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Ustawia wartość odczytu.
     *
     * @param value wartość odczytu
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Zwraca datę odczytu licznika.
     *
     * @return data odczytu
     */
    public LocalDate getReadingDate() {
        return readingDate;
    }

    /**
     * Ustawia datę odczytu licznika.
     *
     * @param readingDate data odczytu
     */
    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    /**
     * Zwraca datę utworzenia rekordu (uzupełniana przez auditing).
     *
     * @return data i czas utworzenia
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Ustawia datę utworzenia rekordu.
     *
     * @param createdAt data i czas utworzenia
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Zwraca datę ostatniej modyfikacji rekordu (uzupełniana przez auditing).
     *
     * @return data i czas ostatniej modyfikacji
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Ustawia datę ostatniej modyfikacji rekordu.
     *
     * @param updatedAt data i czas ostatniej modyfikacji
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Zwraca identyfikator użytkownika, który zapisał odczyt.
     *
     * @return identyfikator użytkownika zapisującego odczyt
     */
    public String getRecordedBy() {
        return recordedBy;
    }

    /**
     * Ustawia identyfikator użytkownika, który zapisał odczyt.
     *
     * @param recordedBy identyfikator użytkownika
     */
    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    /**
     * Informuje, czy odczyt został oznaczony jako usunięty (soft delete).
     *
     * @return {@code true} jeśli odczyt jest usunięty
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Ustawia flagę usunięcia odczytu (soft delete).
     *
     * @param deleted {@code true} aby oznaczyć odczyt jako usunięty
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

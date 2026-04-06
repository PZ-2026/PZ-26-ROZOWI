package pl.edu.ur.blokur.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "meter_readings")
@EntityListeners(AuditingEntityListener::class)
// TODO: Zgodnie z Modułem 4 specyfikacji, liczniki powinny być osobnymi encjami (Meter)
// posiadającymi numer seryjny, datę montażu i typ medium. Obecna implementacja
// używa jedynie prostego pola meterType, co jest niezgodne z "cyfrowym bliźniakiem".
open class MeterReading {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "apartment_id", nullable = false)
    open var apartment: Apartment? = null

    @Column(name = "meter_type", nullable = false, length = 100)
    open var meterType: String? = null

    @Column(name = "value", nullable = false, precision = 12, scale = 4)
    open var value: BigDecimal? = null

    @Column(name = "reading_date", nullable = false)
    open var readingDate: LocalDate? = null

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    open var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at")
    open var updatedAt: LocalDateTime? = null

    @CreatedBy
    @Column(name = "recorded_by", updatable = false, length = 255)
    open var recordedBy: String? = null

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    open var isDeleted: Boolean = false
}

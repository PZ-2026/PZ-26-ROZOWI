package pl.edu.ur.blokur.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "apartments")
open class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Column(name = "number", nullable = false, length = 50)
    open var number: String? = null

    @ColumnDefault("0.00")
    @Column(name = "current_balance", precision = 12, scale = 2)
    open var currentBalance: BigDecimal? = null
}

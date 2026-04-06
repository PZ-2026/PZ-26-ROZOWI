package pl.edu.ur.blokur.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class MeterReadingRequest(
    @field:NotBlank
    val meterType: String,
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Wartość odczytu nie może być ujemna")
    val value: BigDecimal,
    val readingDate: LocalDate,
)

data class MeterReadingResponse(
    val id: UUID,
    val apartmentId: UUID,
    val meterType: String,
    val value: BigDecimal,
    val readingDate: LocalDate,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val recordedBy: String?,
)

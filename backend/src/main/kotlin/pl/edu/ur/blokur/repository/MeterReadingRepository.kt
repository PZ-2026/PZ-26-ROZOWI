package pl.edu.ur.blokur.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import pl.edu.ur.blokur.models.MeterReading
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface MeterReadingRepository : JpaRepository<MeterReading, UUID> {
    fun findByApartmentIdAndIsDeletedFalse(
        apartmentId: UUID,
        pageable: Pageable,
    ): Page<MeterReading>

    fun findByIdAndIsDeletedFalse(id: UUID): Optional<MeterReading>

    fun findTopByApartmentIdAndMeterTypeAndIsDeletedFalseOrderByReadingDateDesc(
        apartmentId: UUID,
        meterType: String,
    ): MeterReading?

    fun existsByApartmentIdAndMeterTypeAndReadingDateAndIsDeletedFalse(
        apartmentId: UUID,
        meterType: String,
        readingDate: LocalDate,
    ): Boolean

    fun existsByApartmentIdAndMeterTypeAndReadingDateAndIdNotAndIsDeletedFalse(
        apartmentId: UUID,
        meterType: String,
        readingDate: LocalDate,
        id: UUID,
    ): Boolean
}

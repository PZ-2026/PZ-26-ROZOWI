package pl.edu.ur.blokur.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import pl.edu.ur.blokur.dto.MeterReadingRequest
import pl.edu.ur.blokur.dto.MeterReadingResponse
import pl.edu.ur.blokur.exception.BusinessValidationException
import pl.edu.ur.blokur.exception.NotFoundException
import pl.edu.ur.blokur.models.MeterReading
import pl.edu.ur.blokur.repository.ApartmentRepository
import pl.edu.ur.blokur.repository.MeterReadingRepository
import java.util.UUID

@Service
class MeterReadingService(
    private val meterReadingRepository: MeterReadingRepository,
    private val apartmentRepository: ApartmentRepository,
) {
    fun create(
        apartmentId: UUID,
        request: MeterReadingRequest,
    ): MeterReadingResponse {
        val apartment =
            apartmentRepository.findById(apartmentId).orElseThrow {
                NotFoundException("Lokal o ID $apartmentId nie istnieje")
            }

        checkDuplicateOnCreate(apartmentId, request)
        checkRegressionOnCreate(apartmentId, request)

        val reading =
            MeterReading().apply {
                this.apartment = apartment
                this.meterType = request.meterType
                this.value = request.value
                this.readingDate = request.readingDate
            }

        return meterReadingRepository.save(reading).toResponse()
    }

    fun getAllByApartment(
        apartmentId: UUID,
        page: Int,
        size: Int,
    ): Page<MeterReadingResponse> {
        // TODO: Implementacja weryfikacji uprawnień (Ownership/Authorization Check):
        // 1. Jeśli rola to MIESZKANIEC -> sprawdź czy jest przypisany do apartmentId.
        // 2. Jeśli rola to KONSERWATOR -> sprawdź czy ma aktywne zlecenie dla tego lokalu.
        if (!apartmentRepository.existsById(apartmentId)) {
            throw NotFoundException("Lokal o ID $apartmentId nie istnieje")
        }
        val pageable = PageRequest.of(page, size, Sort.by("readingDate").descending())
        return meterReadingRepository
            .findByApartmentIdAndIsDeletedFalse(apartmentId, pageable)
            .map { it.toResponse() }
    }

    fun getById(id: UUID): MeterReadingResponse =
        meterReadingRepository.findByIdAndIsDeletedFalse(id).orElseThrow {
            NotFoundException("Odczyt licznika o ID $id nie istnieje")
        }.toResponse()

    fun update(
        id: UUID,
        request: MeterReadingRequest,
    ): MeterReadingResponse {
        val reading =
            meterReadingRepository.findByIdAndIsDeletedFalse(id).orElseThrow {
                NotFoundException("Odczyt licznika o ID $id nie istnieje")
            }

        checkDuplicateOnUpdate(reading.apartment!!.id!!, request, id)
        checkRegressionOnUpdate(reading.apartment!!.id!!, request, id)

        reading.meterType = request.meterType
        reading.value = request.value
        reading.readingDate = request.readingDate

        return meterReadingRepository.save(reading).toResponse()
    }

    fun delete(id: UUID) {
        val reading =
            meterReadingRepository.findByIdAndIsDeletedFalse(id).orElseThrow {
                NotFoundException("Odczyt licznika o ID $id nie istnieje")
            }
        reading.isDeleted = true
        meterReadingRepository.save(reading)
    }

    private fun checkDuplicateOnCreate(
        apartmentId: UUID,
        request: MeterReadingRequest,
    ) {
        if (
            meterReadingRepository.existsByApartmentIdAndMeterTypeAndReadingDateAndIsDeletedFalse(
                apartmentId,
                request.meterType,
                request.readingDate,
            )
        ) {
            throw BusinessValidationException(
                "Odczyt licznika typu '${request.meterType}' dla tego lokalu z datą ${request.readingDate} już istnieje",
            )
        }
    }

    private fun checkDuplicateOnUpdate(
        apartmentId: UUID,
        request: MeterReadingRequest,
        currentId: UUID,
    ) {
        if (
            meterReadingRepository.existsByApartmentIdAndMeterTypeAndReadingDateAndIdNotAndIsDeletedFalse(
                apartmentId,
                request.meterType,
                request.readingDate,
                currentId,
            )
        ) {
            throw BusinessValidationException(
                "Odczyt licznika typu '${request.meterType}' dla tego lokalu z datą ${request.readingDate} już istnieje",
            )
        }
    }

    private fun checkRegressionOnCreate(
        apartmentId: UUID,
        request: MeterReadingRequest,
    ) {
        val latest =
            meterReadingRepository
                .findTopByApartmentIdAndMeterTypeAndIsDeletedFalseOrderByReadingDateDesc(
                    apartmentId,
                    request.meterType,
                ) ?: return

        if (request.value < latest.value!!) {
            throw BusinessValidationException(
                "Nowa wartość odczytu (${request.value}) nie może być mniejsza niż ostatni odczyt (${latest.value}) z dnia ${latest.readingDate}",
            )
        }
    }

    private fun checkRegressionOnUpdate(
        apartmentId: UUID,
        request: MeterReadingRequest,
        currentId: UUID,
    ) {
        val latest =
            meterReadingRepository
                .findTopByApartmentIdAndMeterTypeAndIsDeletedFalseOrderByReadingDateDesc(
                    apartmentId,
                    request.meterType,
                ) ?: return

        if (latest.id != currentId && request.value < latest.value!!) {
            throw BusinessValidationException(
                "Nowa wartość odczytu (${request.value}) nie może być mniejsza niż ostatni odczyt (${latest.value}) z dnia ${latest.readingDate}",
            )
        }
    }

    private fun MeterReading.toResponse() =
        MeterReadingResponse(
            id = id!!,
            apartmentId = apartment!!.id!!,
            meterType = meterType!!,
            value = value!!,
            readingDate = readingDate!!,
            createdAt = createdAt,
            updatedAt = updatedAt,
            recordedBy = recordedBy,
        )
}

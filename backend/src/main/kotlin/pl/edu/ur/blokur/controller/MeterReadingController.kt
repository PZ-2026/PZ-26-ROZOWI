package pl.edu.ur.blokur.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.edu.ur.blokur.dto.MeterReadingRequest
import pl.edu.ur.blokur.dto.MeterReadingResponse
import pl.edu.ur.blokur.service.MeterReadingService
import java.util.UUID

@RestController
@RequestMapping("/api")
class MeterReadingController(
    private val meterReadingService: MeterReadingService,
) {
    // TODO: Zgodnie z Modułem 7, należy zweryfikować czy konserwator jest upoważniony
    // do obsługi tego konkretnego lokalu (np. posiada przypisane zgłoszenie).
    @PostMapping("/apartments/{apartmentId}/meter-readings")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR')")
    fun create(
        @PathVariable apartmentId: UUID,
        @Valid @RequestBody request: MeterReadingRequest,
    ): ResponseEntity<MeterReadingResponse> {
        val response = meterReadingService.create(apartmentId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // TODO: Krytyczny brak weryfikacji - Mieszkaniec może pobrać odczyty DOWOLNEGO lokalu,
    // jeśli zna jego apartmentId. Należy dodać sprawdzenie czy zalogowany użytkownik
    // jest właścicielem/najemcą tego lokalu.
    @GetMapping("/apartments/{apartmentId}/meter-readings")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR', 'MIESZKANIEC')")
    fun getAllByApartment(
        @PathVariable apartmentId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<MeterReadingResponse>> {
        val readings = meterReadingService.getAllByApartment(apartmentId, page, size)
        return ResponseEntity.ok(readings)
    }

    @GetMapping("/meter-readings/{id}")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR', 'MIESZKANIEC')")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<MeterReadingResponse> {
        val reading = meterReadingService.getById(id)
        return ResponseEntity.ok(reading)
    }

    @PutMapping("/meter-readings/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MeterReadingRequest,
    ): ResponseEntity<MeterReadingResponse> {
        val response = meterReadingService.update(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/meter-readings/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        meterReadingService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

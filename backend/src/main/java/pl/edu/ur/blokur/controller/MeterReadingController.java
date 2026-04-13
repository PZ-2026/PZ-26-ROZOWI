package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.MeterReadingRequest;
import pl.edu.ur.blokur.dto.MeterReadingResponse;
import pl.edu.ur.blokur.service.MeterReadingService;

import java.util.UUID;

/**
 * Kontroler REST obsługujący operacje CRUD na odczytach liczników.
 * Dostęp do poszczególnych operacji jest ograniczony na podstawie roli użytkownika.
 */
@RestController
@RequestMapping("/api")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /**
     * Tworzy nowy odczyt licznika dla wskazanego lokalu.
     * Dostęp: ZARZADCA, KONSERWATOR.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowego odczytu
     * @return utworzony odczyt z kodem HTTP 201
     */
    // TODO: Zgodnie z Modułem 7, należy zweryfikować czy konserwator jest upoważniony
    // do obsługi tego konkretnego lokalu (np. posiada przypisane zgłoszenie).
    @PostMapping("/apartments/{apartmentId}/meter-readings")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR')")
    public ResponseEntity<MeterReadingResponse> create(
        @PathVariable UUID apartmentId,
        @Valid @RequestBody MeterReadingRequest request
    ) {
        MeterReadingResponse response = meterReadingService.create(apartmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Pobiera paginowaną listę odczytów dla wskazanego lokalu.
     * Dostęp: ZARZADCA, KONSERWATOR, MIESZKANIEC.
     *
     * @param apartmentId identyfikator lokalu
     * @param page numer strony (domyślnie 0)
     * @param size rozmiar strony (domyślnie 20)
     * @return strona z odczytami liczników
     */
    // TODO: Krytyczny brak weryfikacji — Mieszkaniec może pobrać odczyty DOWOLNEGO lokalu,
    // jeśli zna jego apartmentId. Należy dodać sprawdzenie czy zalogowany użytkownik
    // jest właścicielem/najemcą tego lokalu.
    @GetMapping("/apartments/{apartmentId}/meter-readings")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR', 'MIESZKANIEC')")
    public ResponseEntity<Page<MeterReadingResponse>> getAllByApartment(
        @PathVariable UUID apartmentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<MeterReadingResponse> readings = meterReadingService.getAllByApartment(apartmentId, page, size);
        return ResponseEntity.ok(readings);
    }

    /**
     * Pobiera pojedynczy odczyt licznika na podstawie jego identyfikatora.
     * Dostęp: ZARZADCA, KONSERWATOR, MIESZKANIEC.
     *
     * @param id identyfikator odczytu
     * @return odczyt licznika
     */
    @GetMapping("/meter-readings/{id}")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR', 'MIESZKANIEC')")
    public ResponseEntity<MeterReadingResponse> getById(@PathVariable UUID id) {
        MeterReadingResponse reading = meterReadingService.getById(id);
        return ResponseEntity.ok(reading);
    }

    /**
     * Aktualizuje istniejący odczyt licznika.
     * Dostęp: ZARZADCA.
     *
     * @param id identyfikator odczytu do aktualizacji
     * @param request nowe dane odczytu
     * @return zaktualizowany odczyt
     */
    @PutMapping("/meter-readings/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<MeterReadingResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody MeterReadingRequest request
    ) {
        MeterReadingResponse response = meterReadingService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Usuwa (miękkie usunięcie) odczyt licznika.
     * Dostęp: ZARZADCA.
     *
     * @param id identyfikator odczytu do usunięcia
     * @return odpowiedź HTTP 204 bez treści
     */
    @DeleteMapping("/meter-readings/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        meterReadingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

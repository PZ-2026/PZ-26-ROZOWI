package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.MeterRequest;
import pl.edu.ur.blokur.dto.MeterResponse;
import pl.edu.ur.blokur.service.MeterService;

import java.util.List;
import java.util.UUID;

/**
 * Kontroler REST do zarządzania licznikami przypisanymi do lokali.
 */
@RestController
@RequestMapping("/api")
public class MeterController {

    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    /**
     * Dodaje nowy licznik do wskazanego lokalu.
     * Dostęp: ZARZADCA.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowego licznika
     * @return utworzony licznik z kodem HTTP 201
     */
    @PostMapping("/apartments/{apartmentId}/meters")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<MeterResponse> create(
        @PathVariable UUID apartmentId,
        @Valid @RequestBody MeterRequest request
    ) {
        MeterResponse response = meterService.create(apartmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Zwraca listę liczników przypisanych do wskazanego lokalu.
     * Dostęp: ZARZADCA, KONSERWATOR, MIESZKANIEC.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista liczników lokalu
     */
    @GetMapping("/apartments/{apartmentId}/meters")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'KONSERWATOR', 'MIESZKANIEC')")
    public ResponseEntity<List<MeterResponse>> getAllByApartment(@PathVariable UUID apartmentId) {
        List<MeterResponse> meters = meterService.getAllByApartment(apartmentId);
        return ResponseEntity.ok(meters);
    }

    /**
     * Dezaktywuje wskazany licznik (ustawia is_active = false).
     * Dostęp: ZARZADCA.
     *
     * @param id identyfikator licznika
     * @return zdezaktywowany licznik
     */
    @PatchMapping("/meters/{id}/deactivate")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<MeterResponse> deactivate(@PathVariable UUID id) {
        MeterResponse response = meterService.deactivate(id);
        return ResponseEntity.ok(response);
    }
}

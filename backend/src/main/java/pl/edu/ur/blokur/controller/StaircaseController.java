package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.ApartmentRequest;
import pl.edu.ur.blokur.dto.ApartmentResponse;
import pl.edu.ur.blokur.service.BuildingService;

/** Kontroler zarządzający lokalami mieszkalnymi w obrębie klatki schodowej. */
@RestController
@RequestMapping("/api/staircases")
@PreAuthorize("hasRole('ZARZADCA')")
public class StaircaseController {

    private final BuildingService buildingService;

    public StaircaseController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    /**
     * Tworzy nowy lokal w klatce schodowej. Dostęp: ZARZADCA.
     *
     * @param id identyfikator klatki schodowej
     * @param request dane nowego lokalu
     * @return utworzony lokal z kodem HTTP 201
     */
    @PostMapping("/{id}/apartments")
    public ResponseEntity<ApartmentResponse> createApartment(
            @PathVariable UUID id, @Valid @RequestBody ApartmentRequest request) {
        ApartmentResponse response = buildingService.createApartment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Aktualizuje dane lokalu. Dostęp: ZARZADCA.
     *
     * @param id identyfikator klatki schodowej
     * @param aptId identyfikator lokalu
     * @param request nowe dane lokalu
     * @return zaktualizowany lokal z kodem HTTP 200
     */
    @PutMapping("/{id}/apartments/{aptId}")
    public ResponseEntity<ApartmentResponse> updateApartment(
            @PathVariable UUID id,
            @PathVariable UUID aptId,
            @Valid @RequestBody ApartmentRequest request) {
        ApartmentResponse response = buildingService.updateApartment(id, aptId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Usuwa lokal. Historyczne zgłoszenia powiązane z lokalem pozostają nienaruszone.
     * Dostęp: ZARZADCA.
     *
     * @param id identyfikator klatki schodowej
     * @param aptId identyfikator lokalu
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}/apartments/{aptId}")
    public ResponseEntity<Void> deleteApartment(@PathVariable UUID id, @PathVariable UUID aptId) {
        buildingService.deleteApartment(id, aptId);
        return ResponseEntity.noContent().build();
    }
}

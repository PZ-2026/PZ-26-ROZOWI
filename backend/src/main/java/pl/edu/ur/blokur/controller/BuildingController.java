package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.BuildingRequest;
import pl.edu.ur.blokur.dto.BuildingResponse;
import pl.edu.ur.blokur.dto.BuildingTreeDto;
import pl.edu.ur.blokur.dto.StaircaseRequest;
import pl.edu.ur.blokur.dto.StaircaseResponse;
import pl.edu.ur.blokur.service.BuildingService;

/** Kontroler zarządzający API dla budynków i hierarchii osiedla. */
@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    /**
     * Endpoint zwracający zagnieżdżoną strukturę drzewa: Budynek -> Klatki -> Lokale.
     *
     * @return hierarchiczne dane budynków
     */
    @GetMapping("/tree")
    public ResponseEntity<List<BuildingTreeDto>> getBuildingTree() {
        return ResponseEntity.ok(buildingService.getBuildingTree());
    }

    /**
     * Tworzy nowy budynek. Dostęp: ZARZADCA.
     *
     * @param request dane nowego budynku
     * @return utworzony budynek z kodem HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<BuildingResponse> createBuilding(
            @Valid @RequestBody BuildingRequest request) {
        BuildingResponse response = buildingService.createBuilding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Aktualizuje dane istniejącego budynku. Dostęp: ZARZADCA.
     *
     * @param id identyfikator budynku
     * @param request nowe dane budynku
     * @return zaktualizowany budynek z kodem HTTP 200
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @PathVariable UUID id, @Valid @RequestBody BuildingRequest request) {
        BuildingResponse response = buildingService.updateBuilding(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Usuwa budynek (tylko gdy nie ma powiązanych lokali). Dostęp: ZARZADCA.
     *
     * @param id identyfikator budynku
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<Void> deleteBuilding(@PathVariable UUID id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Tworzy nową klatkę schodową w budynku. Dostęp: ZARZADCA.
     *
     * @param id identyfikator budynku
     * @param request dane nowej klatki
     * @return utworzona klatka z kodem HTTP 201
     */
    @PostMapping("/{id}/staircases")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<StaircaseResponse> createStaircase(
            @PathVariable UUID id, @Valid @RequestBody StaircaseRequest request) {
        StaircaseResponse response = buildingService.createStaircase(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Aktualizuje dane klatki schodowej. Dostęp: ZARZADCA.
     *
     * @param id identyfikator budynku
     * @param stId identyfikator klatki
     * @param request nowe dane klatki
     * @return zaktualizowana klatka z kodem HTTP 200
     */
    @PutMapping("/{id}/staircases/{stId}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<StaircaseResponse> updateStaircase(
            @PathVariable UUID id,
            @PathVariable UUID stId,
            @Valid @RequestBody StaircaseRequest request) {
        StaircaseResponse response = buildingService.updateStaircase(id, stId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Usuwa klatkę schodową (tylko gdy nie ma powiązanych lokali). Dostęp: ZARZADCA.
     *
     * @param id identyfikator budynku
     * @param stId identyfikator klatki
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}/staircases/{stId}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<Void> deleteStaircase(@PathVariable UUID id, @PathVariable UUID stId) {
        buildingService.deleteStaircase(id, stId);
        return ResponseEntity.noContent().build();
    }
}

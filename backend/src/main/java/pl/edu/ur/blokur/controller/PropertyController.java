package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.PropertyRequest;
import pl.edu.ur.blokur.dto.PropertyResponse;
import pl.edu.ur.blokur.service.FileTypeValidator;
import pl.edu.ur.blokur.service.PropertyService;

/**
 * Kontroler REST do zarządzania nieruchomościami (wspólnotami mieszkaniowymi). Wszystkie endpointy
 * są dostępne wyłącznie dla roli ZARZADCA.
 */
@RestController
@RequestMapping("/api/properties")
@PreAuthorize("hasRole('ZARZADCA')")
public class PropertyController {

    private final PropertyService propertyService;
    private final FileTypeValidator fileTypeValidator;

    /**
     * Konstruktor z wstrzyknięciem serwisu nieruchomości.
     *
     * @param propertyService serwis zarządzający nieruchomościami
     * @param fileTypeValidator walidator typów plików
     */
    public PropertyController(
            PropertyService propertyService, FileTypeValidator fileTypeValidator) {
        this.propertyService = propertyService;
        this.fileTypeValidator = fileTypeValidator;
    }

    /**
     * Tworzy nową nieruchomość. Dostęp: ZARZADCA.
     *
     * @param request dane nowej nieruchomości
     * @return utworzona nieruchomość z kodem HTTP 201
     */
    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse response = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Aktualizuje dane istniejącej nieruchomości. Dostęp: ZARZADCA.
     *
     * @param id identyfikator nieruchomości
     * @param request nowe dane nieruchomości
     * @return zaktualizowana nieruchomość z kodem HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable UUID id, @Valid @RequestBody PropertyRequest request) {
        PropertyResponse response = propertyService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Zwraca listę wszystkich nieruchomości. Dostęp: ZARZADCA.
     *
     * @return lista nieruchomości z kodem HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAll() {
        List<PropertyResponse> properties = propertyService.getAll();
        return ResponseEntity.ok(properties);
    }

    /**
     * Zwraca dane wskazanej nieruchomości. Dostęp: ZARZADCA.
     *
     * @param id identyfikator nieruchomości
     * @return nieruchomość z kodem HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable UUID id) {
        PropertyResponse response = propertyService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Przesyła i zapisuje logo nieruchomości (PNG lub JPEG, max 2 MB). Ścieżka do pliku logo jest
     * używana w nagłówkach generowanych dokumentów PDF. Dostęp: ZARZADCA.
     *
     * @param id identyfikator nieruchomości
     * @param file plik graficzny logo (PNG lub JPEG)
     * @return nieruchomość z zaktualizowaną ścieżką logo i kodem HTTP 200
     */
    @PatchMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> uploadLogo(
            @PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        fileTypeValidator.validateImage(file);
        PropertyResponse response = propertyService.uploadLogo(id, file);
        return ResponseEntity.ok(response);
    }
}

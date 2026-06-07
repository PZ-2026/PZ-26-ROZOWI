package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
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
import pl.edu.ur.blokur.dto.InspectionRequest;
import pl.edu.ur.blokur.dto.InspectionResponse;
import pl.edu.ur.blokur.service.InspectionService;

/**
 * Kontroler obsługujący żądania HTTP dla modułu przeglądów technicznych. Tworzenie, edycja i
 * usuwanie przeglądów dostępne wyłącznie dla roli ZARZADCA. Odczyt dostępny dla wszystkich
 * uwierzytelnionych użytkowników — wyniki filtrowane po zasięgu.
 */
@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    /**
     * Tworzy instancję kontrolera z wymaganym serwisem.
     *
     * @param inspectionService serwis logiki biznesowej przeglądów
     */
    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    /**
     * Tworzy nowy przegląd techniczny. Dostępne wyłącznie dla roli ZARZADCA.
     *
     * @param request dane nowego przeglądu
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return utworzony przegląd z kodem 201, lub 404 gdy encja zasięgu nie istnieje
     */
    @PostMapping
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<InspectionResponse> create(
            @Valid @RequestBody InspectionRequest request, Principal principal) {
        InspectionResponse response = inspectionService.create(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Zwraca listę przeglądów filtrowaną według zasięgu zalogowanego użytkownika. Zarządca widzi
     * wszystkie przeglądy; pozostałe role — przeglądy dla swojej klatki, budynku i nieruchomości.
     *
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return lista przeglądów (HTTP 200) lub 403 gdy brak autoryzacji
     */
    @GetMapping
    public ResponseEntity<List<InspectionResponse>> getAll(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(inspectionService.getAll(principal.getName()));
    }

    /**
     * Aktualizuje istniejący przegląd techniczny. Dostępne wyłącznie dla roli ZARZADCA.
     *
     * @param id identyfikator przeglądu do aktualizacji
     * @param request nowe dane przeglądu
     * @return zaktualizowany przegląd z kodem 200, lub 404 gdy przegląd/zasięg nie istnieje
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<InspectionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody InspectionRequest request) {
        InspectionResponse response = inspectionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Usuwa przegląd techniczny na podstawie identyfikatora. Dostępne wyłącznie dla roli ZARZADCA.
     *
     * @param id identyfikator przeglądu do usunięcia
     * @return kod 204 po sukcesie, lub 404 gdy przegląd nie istnieje
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        inspectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

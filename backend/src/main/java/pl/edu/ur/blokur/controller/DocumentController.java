package pl.edu.ur.blokur.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.service.DocumentService;

/** Kontroler odpowiedzialny za listowanie i pobieranie dokumentów. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Konstruktor kontrolera.
     *
     * @param documentService serwis dokumentów
     */
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Zwraca listę dokumentów. Zależnie od roli (ZARZADCA / MIESZKANIEC) stosowane są inne reguły
     * widoczności.
     *
     * @param apartmentId (opcjonalny) identyfikator mieszkania
     * @param startDate (opcjonalny) data początkowa
     * @param endDate (opcjonalny) data końcowa
     * @param type (opcjonalny) typ dokumentu
     * @param principal zalogowany użytkownik
     * @return lista dokumentów (200 OK) lub 403 w przypadku braku uprawnień
     */
    @GetMapping
    public ResponseEntity<List<DocumentDto>> getDocuments(
            @RequestParam(required = false) UUID apartmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @RequestParam(required = false) String type,
            Principal principal) {
        try {
            List<DocumentDto> documents =
                    documentService.getDocuments(apartmentId, startDate, endDate, type, principal.getName());
            return ResponseEntity.ok(documents);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Służy do pobrania zawartości dokumentu. Jeśli użytkownik nie posiada dostępu, zwróci 403
     * Forbidden.
     *
     * @param id identyfikator dokumentu
     * @param principal zalogowany użytkownik
     * @return plik PDF
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id, Principal principal) {
        try {
            Resource resource = documentService.downloadDocument(id, principal.getName());

            String filename =
                    resource.getFilename() != null ? resource.getFilename() : "document.pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}

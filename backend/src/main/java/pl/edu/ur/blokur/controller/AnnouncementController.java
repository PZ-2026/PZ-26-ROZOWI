package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.dto.AnnouncementRequest;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.repository.AnnouncementRepository;
import pl.edu.ur.blokur.service.AnnouncementService;
import pl.edu.ur.blokur.service.FileTypeValidator;

/**
 * Kontroler obsługujący żądania HTTP dla modułu ogłoszeń. Udostępnia endpointy CRUD dla zarządcy
 * oraz endpoint GET dla mieszkańców.
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementRepository announcementRepository;
    private final FileTypeValidator fileTypeValidator;

    /**
     * Tworzy instancję kontrolera z wymaganymi zależnościami.
     *
     * @param announcementService serwis logiki biznesowej ogłoszeń
     * @param announcementRepository repozytorium ogłoszeń
     * @param fileTypeValidator walidator typów plików
     */
    public AnnouncementController(
            AnnouncementService announcementService,
            AnnouncementRepository announcementRepository,
            FileTypeValidator fileTypeValidator) {
        this.announcementService = announcementService;
        this.announcementRepository = announcementRepository;
        this.fileTypeValidator = fileTypeValidator;
    }

    /**
     * Zwraca listę ogłoszeń dopasowanych do zalogowanego użytkownika. Ogłoszenia starsze niż 12
     * miesięcy nie są zwracane.
     *
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return lista ogłoszeń (HTTP 200) lub błąd 403 gdy brak autoryzacji
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementDto>> getAnnouncements(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        List<AnnouncementDto> dtos =
                announcementService.getAnnouncementsForUser(principal.getName());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Tworzy nowe ogłoszenie. Dostępne tylko dla zarządcy. Przyjmuje dane w formacie
     * multipart/form-data.
     *
     * @param request dane ogłoszenia (JSON)
     * @param attachment opcjonalny załącznik PDF
     * @return utworzone ogłoszenie (HTTP 201)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<AnnouncementDto> createAnnouncement(
            @Valid @RequestPart("data") AnnouncementRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            Authentication auth) {
        if (attachment != null && !attachment.isEmpty()) {
            fileTypeValidator.validatePdf(attachment);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcementService.createAnnouncement(request, attachment, auth.getName()));
    }

    /**
     * Aktualizuje istniejące ogłoszenie. Dostępne tylko dla zarządcy.
     *
     * @param id identyfikator ogłoszenia
     * @param request zaktualizowane dane (JSON)
     * @param attachment opcjonalny nowy załącznik PDF
     * @return zaktualizowane ogłoszenie (HTTP 200)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(
            @PathVariable UUID id,
            @Valid @RequestPart("data") AnnouncementRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            Authentication auth) {
        if (attachment != null && !attachment.isEmpty()) {
            fileTypeValidator.validatePdf(attachment);
        }
        return ResponseEntity.ok(
                announcementService.updateAnnouncement(id, request, attachment, auth.getName()));
    }

    /**
     * Usuwa ogłoszenie. Dostępne tylko dla zarządcy.
     *
     * @param id identyfikator ogłoszenia
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable UUID id, Authentication auth) {
        announcementService.deleteAnnouncement(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera załącznik PDF ogłoszenia.
     *
     * @param id identyfikator ogłoszenia
     * @return plik PDF jako bajty
     */
    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> getAttachment(@PathVariable UUID id) {
        var announcement =
                announcementRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("Ogłoszenie nie istnieje"));

        if (announcement.getAttachmentPath() == null) {
            throw new NotFoundException("Ogłoszenie nie posiada załącznika");
        }

        try {
            Path filePath = Paths.get(announcement.getAttachmentPath());
            byte[] bytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filePath.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas odczytu załącznika", e);
        }
    }
}

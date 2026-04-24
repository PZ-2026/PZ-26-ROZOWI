package pl.edu.ur.blokur.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.TicketImageDto;
import pl.edu.ur.blokur.models.TicketImageType;
import pl.edu.ur.blokur.service.TicketImageService;

/** Kontroler obsługujący operacje na zdjęciach w systemie zgłoszeń. */
@RestController
@RequestMapping("/api")
public class TicketImageController {

    private final TicketImageService ticketImageService;

    public TicketImageController(TicketImageService ticketImageService) {
        this.ticketImageService = ticketImageService;
    }

    /** Wgrywa nowe zdjęcie do zgłoszenia. */
    @PostMapping(value = "/tickets/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MIESZKANIEC', 'KONSERWATOR', 'ZARZADCA')")
    public ResponseEntity<TicketImageDto> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("image_type") TicketImageType imageType,
            Authentication auth) {
        return ResponseEntity.ok(
                ticketImageService.uploadImage(id, file, imageType, auth.getName()));
    }

    /** Pobiera listę zdjęć powiązanych ze zgłoszeniem. */
    @GetMapping("/tickets/{id}/images")
    @PreAuthorize("hasAnyRole('MIESZKANIEC', 'KONSERWATOR', 'ZARZADCA')")
    public ResponseEntity<List<TicketImageDto>> getImagesForTicket(
            @PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(ticketImageService.getImagesForTicket(id, auth.getName()));
    }

    /** Serwuje plik obrazka z dysku serwera. */
    @GetMapping("/images/{id}")
    @PreAuthorize("hasAnyRole('MIESZKANIEC', 'KONSERWATOR', 'ZARZADCA')")
    public ResponseEntity<Resource> serveImage(@PathVariable UUID id, Authentication auth) {
        Resource resource = ticketImageService.serveImage(id, auth.getName());

        String contentType = "application/octet-stream";
        if (resource.getFilename() != null) {
            if (resource.getFilename().toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (resource.getFilename().toLowerCase().endsWith(".jpg")
                    || resource.getFilename().toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

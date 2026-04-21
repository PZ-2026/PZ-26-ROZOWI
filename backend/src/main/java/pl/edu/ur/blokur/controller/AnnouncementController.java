package pl.edu.ur.blokur.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.service.AnnouncementService;

/**
 * Kontroler obsługujący żądania HTTP dla modułu ogłoszeń.
 * Udostępnia endpoint chroniony tokenem JWT, zwracający ogłoszenia
 * właściwe dla aktualnie zalogowanego użytkownika.
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * Tworzy instancję kontrolera z wymaganym serwisem.
     *
     * @param announcementService serwis logiki biznesowej ogłoszeń
     */
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * Zwraca listę ogłoszeń dopasowanych do zalogowanego użytkownika.
     * Ogłoszenia filtrowane są na podstawie powiązania rezydenta
     * z lokalem, klatką i budynkiem.
     *
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return lista ogłoszeń (HTTP 200) lub błąd 403 gdy brak autoryzacji
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementDto>> getAnnouncements(
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        List<AnnouncementDto> dtos = announcementService
                .getAnnouncementsForUser(principal.getName());

        return ResponseEntity.ok(dtos);
    }
}

package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.dto.TicketAssignRequest;
import pl.edu.ur.blokur.dto.TicketRejectRequest;
import pl.edu.ur.blokur.service.TicketService;
import org.springframework.web.bind.annotation.PatchMapping;

/**
 * Kontroler obsługujący żądania HTTP dla modułu zgłoszeń. Tworzenie zgłoszeń dostępne wyłącznie dla
 * roli MIESZKANIEC. Odczyt dostępny dla wszystkich uwierzytelnionych użytkowników — wyniki
 * filtrowane według roli (ZARZADCA widzi wszystkie, KONSERWATOR swoje, MIESZKANIEC lokalu).
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Tworzy instancję kontrolera z wymaganym serwisem.
     *
     * @param ticketService serwis logiki biznesowej zgłoszeń
     */
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Tworzy nowe zgłoszenie. Lokal jest pobierany automatycznie z konta zalogowanego mieszkańca.
     * Dostępne wyłącznie dla roli MIESZKANIEC.
     *
     * @param request dane nowego zgłoszenia (tytuł, opis, kategoria)
     * @return utworzone zgłoszenie z kodem 201, lub 400 przy błędzie walidacji, 404 gdy brak
     *     kategorii, 422 gdy mieszkaniec nie ma lokalu
     */
    @PostMapping
    @PreAuthorize("hasRole('MIESZKANIEC')")
    public ResponseEntity<TicketDetailDto> create(@Valid @RequestBody TicketRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TicketDetailDto response = ticketService.create(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Zwraca listę zgłoszeń filtrowaną według roli zalogowanego użytkownika i podanych parametrów.
     * ZARZADCA widzi wszystkie; KONSERWATOR widzi tylko przypisane do siebie; MIESZKANIEC widzi
     * tylko zgłoszenia swojego lokalu, klatki i budynku.
     *
     * @param status filtr statusu, np. NOWE
     * @param categoryId filtr kategorii (UUID)
     * @param buildingId filtr budynku (UUID)
     * @param staircaseId filtr klatki schodowej (UUID)
     * @param assignedTo filtr konserwatora (UUID)
     * @param dateFrom dolna granica daty utworzenia (ISO format)
     * @param dateTo górna granica daty utworzenia (ISO format)
     * @param search fraza fulltext przeszukiwana w numerze, tytule i opisie
     * @return lista zgłoszeń z kodem 200, lub 403 gdy brak uwierzytelnienia
     */
    @GetMapping
    public ResponseEntity<List<TicketSummaryDto>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) UUID staircaseId,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime dateTo,
            @RequestParam(required = false) String search) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TicketFilterParams filters = new TicketFilterParams();
        filters.setStatus(status);
        filters.setCategoryId(categoryId);
        filters.setBuildingId(buildingId);
        filters.setStaircaseId(staircaseId);
        filters.setAssignedTo(assignedTo);
        filters.setDateFrom(dateFrom);
        filters.setDateTo(dateTo);
        filters.setSearch(search);

        return ResponseEntity.ok(ticketService.getAll(auth.getName(), filters));
    }

    /**
     * Zwraca szczegóły zgłoszenia z kontrolą uprawnień. MIESZKANIEC nie widzi notatki wewnętrznej.
     * Konserwator może zobaczyć tylko zgłoszenia przypisane do siebie.
     *
     * @param id identyfikator zgłoszenia (UUID)
     * @return szczegóły zgłoszenia z kodem 200, lub 404 gdy nie istnieje, 422 przy braku uprawnień
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDto> getById(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(ticketService.getById(id, auth.getName()));
    }

    /**
     * Przypisuje konserwatora do zgłoszenia. Zmienia status na ZAPLANOWANO.
     * Dostępne tylko dla roli ZARZADCA.
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> assignTicket(
            @PathVariable UUID id, @Valid @RequestBody TicketAssignRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.assignTicket(id, request, auth.getName()));
    }

    /**
     * Zamyka zgłoszenie i generuje protokół PDF.
     * Dostępne tylko dla roli ZARZADCA.
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> closeTicket(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.closeTicket(id, auth.getName()));
    }

    /**
     * Odrzuca zgłoszenie z podaniem powodu. Zmienia status na ODRZUCONE.
     * Dostępne tylko dla roli ZARZADCA.
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> rejectTicket(
            @PathVariable UUID id, @Valid @RequestBody TicketRejectRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.rejectTicket(id, request, auth.getName()));
    }
}

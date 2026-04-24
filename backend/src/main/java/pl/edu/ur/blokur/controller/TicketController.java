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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.TicketAssignRequest;
import pl.edu.ur.blokur.dto.TicketCompletionRequest;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRejectRequest;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketStatusChangeRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.dto.TicketSuspendRequest;
import pl.edu.ur.blokur.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MIESZKANIEC')")
    public ResponseEntity<TicketDetailDto> create(@Valid @RequestBody TicketRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TicketDetailDto response = ticketService.create(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDto> getById(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(ticketService.getById(id, auth.getName()));
    }

    /**
     * Przypisuje konserwatora do zgłoszenia. Zmienia status na ZAPLANOWANO. Dostępne tylko dla roli
     * ZARZADCA.
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> assignTicket(
            @PathVariable UUID id, @Valid @RequestBody TicketAssignRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.assignTicket(id, request, auth.getName()));
    }

    /** Zamyka zgłoszenie i generuje protokół PDF. Dostępne tylko dla roli ZARZADCA. */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> closeTicket(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.closeTicket(id, auth.getName()));
    }

    /**
     * Odrzuca zgłoszenie z podaniem powodu. Zmienia status na ODRZUCONE. Dostępne tylko dla roli
     * ZARZADCA.
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<TicketDetailDto> rejectTicket(
            @PathVariable UUID id, @Valid @RequestBody TicketRejectRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.rejectTicket(id, request, auth.getName()));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('KONSERWATOR')")
    public ResponseEntity<TicketDetailDto> startWork(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.startWork(id, auth.getName()));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('KONSERWATOR')")
    public ResponseEntity<TicketDetailDto> suspendWork(
            @PathVariable UUID id, @Valid @RequestBody TicketSuspendRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.suspendWork(id, request, auth.getName()));
    }

    @PostMapping("/{id}/completion")
    @PreAuthorize("hasRole('KONSERWATOR')")
    public ResponseEntity<TicketDetailDto> completeWork(
            @PathVariable UUID id, @Valid @RequestBody TicketCompletionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.completeWork(id, request, auth.getName()));
    }

    /**
     * Zmienia status zgłoszenia z walidacją state-machine. Dostępne dla KONSERWATORA (własne
     * zgłoszenia: W_REALIZACJI, WSTRZYMANO, ZAKONCZONE_DO_WERYFIKACJI) i ZARZĄDCY.
     *
     * @param id identyfikator zgłoszenia
     * @param request nowy status i opcjonalny komentarz
     * @return zaktualizowane zgłoszenie z kodem 200
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ZARZADCA') or hasRole('KONSERWATOR')")
    public ResponseEntity<TicketDetailDto> changeStatus(
            @PathVariable UUID id, @Valid @RequestBody TicketStatusChangeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ticketService.changeStatus(id, request, auth.getName()));
    }
}

package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.TicketCommentDto;
import pl.edu.ur.blokur.dto.TicketCommentRequest;
import pl.edu.ur.blokur.service.TicketCommentService;

@RestController
@RequestMapping("/api/tickets")
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService) {
        this.ticketCommentService = ticketCommentService;
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('ZARZADCA') or hasRole('KONSERWATOR') or hasRole('MIESZKANIEC')")
    public ResponseEntity<TicketCommentDto> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCommentRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketCommentService.addComment(id, request, auth.getName()));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('ZARZADCA') or hasRole('KONSERWATOR') or hasRole('MIESZKANIEC')")
    public ResponseEntity<List<TicketCommentDto>> getComments(
            @PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(ticketCommentService.getComments(id, auth.getName()));
    }
}

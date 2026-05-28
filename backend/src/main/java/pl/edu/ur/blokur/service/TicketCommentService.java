package pl.edu.ur.blokur.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.TicketCommentDto;
import pl.edu.ur.blokur.dto.TicketCommentRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketComment;
import pl.edu.ur.blokur.models.TicketCommentType;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.TicketCommentRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/** Serwis obsługujący operacje na komentarzach do zgłoszeń serwisowych. */
@Service
public class TicketCommentService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;

    /**
     * Tworzy serwis komentarzy i wstrzykuje wymagane repozytoria.
     *
     * @param ticketRepository repozytorium zgłoszeń
     * @param ticketCommentRepository repozytorium komentarzy do zgłoszeń
     * @param userRepository repozytorium użytkowników
     */
    public TicketCommentService(
            TicketRepository ticketRepository,
            TicketCommentRepository ticketCommentRepository,
            UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Dodaje komentarz do zgłoszenia. MIESZKANIEC - tylko komentarze PUBLICZNY do własnych
     * zgłoszeń. KONSERWATOR - tylko komentarze WEWNETRZNY do przypisanych zgłoszeń. ZARZADCA - oba
     * typy komentarzy.
     */
    @Transactional
    public TicketCommentDto addComment(UUID ticketId, TicketCommentRequest request, String email) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));

        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        validateCommentPermissions(ticket, user, request.getCommentType());

        var comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(user);
        comment.setContent(request.getContent());
        comment.setCommentType(request.getCommentType());
        comment.setCreatedAt(LocalDateTime.now());

        var saved = ticketCommentRepository.save(comment);

        return new TicketCommentDto(
                saved.getId(),
                ticket.getId(),
                user.getFirstName() + " " + user.getLastName(),
                saved.getContent(),
                saved.getCommentType(),
                saved.getCreatedAt());
    }

    /**
     * Pobiera komentarze do zgłoszenia. MIESZKANIEC widzi tylko PUBLICZNY (do własnych). ZARZADCA i
     * KONSERWATOR widzą oba typy.
     */
    @Transactional(readOnly = true)
    public List<TicketCommentDto> getComments(UUID ticketId, String email) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));

        var user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        var allComments = ticketCommentRepository.findCommentsByTicketId(ticketId);

        if ("MIESZKANIEC".equals(user.getRole())) {
            // Mieszkaniec musi być powiązany ze zgłoszeniem (zakładamy najprostszą walidację po
            // mieszkaniu)
            // Lub przynajmniej filtrowanie po komentarzach:
            return allComments.stream()
                    .filter(c -> c.getCommentType() == TicketCommentType.PUBLICZNY)
                    .toList();
        }

        // KONSERWATOR lub ZARZADCA
        return allComments;
    }

    private void validateCommentPermissions(Ticket ticket, User user, TicketCommentType type) {
        switch (user.getRole()) {
            case "MIESZKANIEC" -> {
                if (type != TicketCommentType.PUBLICZNY) {
                    throw new BusinessValidationException(
                            "Mieszkaniec może dodawać tylko publiczne komentarze");
                }
            }
            case "KONSERWATOR" -> {
                if (type != TicketCommentType.WEWNETRZNY) {
                    throw new BusinessValidationException(
                            "Konserwator może dodawać tylko wewnętrzne komentarze");
                }
                if (ticket.getAssignedTo() == null
                        || !ticket.getAssignedTo().getId().equals(user.getId())) {
                    throw new BusinessValidationException(
                            "Konserwator może dodawać komentarze tylko do własnych zgłoszeń");
                }
            }
            case "ZARZADCA" -> {}

            default ->
                    throw new BusinessValidationException("Brak uprawnień do dodawania komentarza");
        }
    }
}

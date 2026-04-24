package pl.edu.ur.blokur.service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.TicketAssignRequest;
import pl.edu.ur.blokur.dto.TicketCompletionRequest;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRejectRequest;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketStatusChangeRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.dto.TicketSuspendRequest;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketCategory;
import pl.edu.ur.blokur.models.TicketHistory;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.DocumentRepository;
import pl.edu.ur.blokur.repository.TicketCategoryRepository;
import pl.edu.ur.blokur.repository.TicketHistoryRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final DocumentRepository documentRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final TicketStateMachine ticketStateMachine;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketCategoryRepository ticketCategoryRepository,
            TicketNumberGenerator ticketNumberGenerator,
            TicketHistoryRepository ticketHistoryRepository,
            DocumentRepository documentRepository,
            PdfGeneratorService pdfGeneratorService,
            TicketStateMachine ticketStateMachine) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.documentRepository = documentRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.ticketStateMachine = ticketStateMachine;
    }

    @PostConstruct
    public void initTicketNumberGenerator() {
        int year = LocalDate.now().getYear();
        int lastSeq = ticketRepository.findMaxSequenceForYear(year);
        ticketNumberGenerator.initYear(year, lastSeq);
    }

    @Transactional
    public TicketDetailDto create(TicketRequest request, String username) {
        User author =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (author.getUserApartments().isEmpty()) {
            throw new BusinessValidationException(
                    "Mieszkaniec nie ma przypisanego lokalu — nie można złożyć zgłoszenia");
        }

        Apartment apartment = author.getUserApartments().get(0).getApartment();

        TicketCategory category =
                ticketCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Kategoria o ID "
                                                        + request.getCategoryId()
                                                        + " nie istnieje"));

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumberGenerator.generate());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.NOWE);
        ticket.setCategory(category);
        ticket.setAuthor(author);
        ticket.setApartment(apartment);
        ticket.setCreatedAt(LocalDateTime.now());

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryDto> getAll(String username, TicketFilterParams filters) {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        String role = user.getRole();

        if ("ZARZADCA".equals(role)) {
            return ticketRepository
                    .findWithFilters(
                            filters.getStatus(),
                            filters.getCategoryId(),
                            filters.getBuildingId(),
                            filters.getStaircaseId(),
                            filters.getAssignedTo(),
                            filters.getDateFrom(),
                            filters.getDateTo(),
                            filters.getSearch())
                    .stream()
                    .map(this::mapRawToSummary)
                    .collect(Collectors.toList());
        }

        if ("KONSERWATOR".equals(role)) {
            return ticketRepository
                    .findForConservatorWithFilters(
                            user.getId(),
                            filters.getStatus(),
                            filters.getCategoryId(),
                            filters.getDateFrom(),
                            filters.getDateTo(),
                            filters.getSearch())
                    .stream()
                    .map(this::mapRawToSummary)
                    .collect(Collectors.toList());
        }

        if (user.getUserApartments().isEmpty()) {
            return List.of();
        }

        UserApartment ua = user.getUserApartments().get(0);
        Apartment apt = ua.getApartment();
        UUID apartmentId = apt != null ? apt.getId() : null;
        UUID staircaseId =
                (apt != null && apt.getStaircase() != null) ? apt.getStaircase().getId() : null;
        UUID buildingId =
                (apt != null
                                && apt.getStaircase() != null
                                && apt.getStaircase().getBuilding() != null)
                        ? apt.getStaircase().getBuilding().getId()
                        : null;

        if (apartmentId == null) {
            return List.of();
        }

        return ticketRepository
                .findForResidentWithFilters(
                        apartmentId,
                        staircaseId,
                        buildingId,
                        filters.getStatus(),
                        filters.getCategoryId(),
                        filters.getDateFrom(),
                        filters.getDateTo(),
                        filters.getSearch())
                .stream()
                .map(this::mapRawToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getById(UUID ticketId, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Zgłoszenie o ID " + ticketId + " nie istnieje"));

        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        String role = user.getRole();

        if ("ZARZADCA".equals(role)) {
            return mapToDetail(ticket);
        }

        if ("KONSERWATOR".equals(role)) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(user.getId())) {
                throw new BusinessValidationException(
                        "Brak dostępu do zgłoszenia — nie jest przypisane do tego konserwatora");
            }
            return mapToDetail(ticket);
        }

        if (user.getUserApartments().isEmpty()) {
            throw new BusinessValidationException(
                    "Brak dostępu do zgłoszenia — mieszkaniec nie ma przypisanego lokalu");
        }

        Apartment apt = user.getUserApartments().get(0).getApartment();
        UUID residentApartmentId = apt != null ? apt.getId() : null;
        UUID residentStaircaseId =
                (apt != null && apt.getStaircase() != null) ? apt.getStaircase().getId() : null;
        UUID residentBuildingId =
                (apt != null
                                && apt.getStaircase() != null
                                && apt.getStaircase().getBuilding() != null)
                        ? apt.getStaircase().getBuilding().getId()
                        : null;

        boolean hasAccess =
                isTicketVisibleForResident(
                        ticket, residentApartmentId, residentStaircaseId, residentBuildingId);

        if (!hasAccess) {
            throw new BusinessValidationException(
                    "Brak dostępu do zgłoszenia — nie dotyczy lokalu tego mieszkańca");
        }

        TicketDetailDto dto = mapToDetail(ticket);
        dto.setInternalNote(null);
        return dto;
    }

    @Transactional
    public TicketDetailDto assignTicket(
            UUID ticketId, TicketAssignRequest request, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User manager =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(manager.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może przypisać konserwatora.");
        }

        User conservator =
                userRepository
                        .findById(request.getAssignedTo())
                        .orElseThrow(() -> new NotFoundException("Konserwator nie istnieje"));

        if (!"KONSERWATOR".equals(conservator.getRole())) {
            throw new BusinessValidationException("Wybrany użytkownik nie jest konserwatorem.");
        }

        ticket.setAssignedTo(conservator);
        ticket.setPlannedVisitAt(request.getPlannedVisitAt());
        ticket.setInternalNote(request.getInternalNote());
        ticket.setStatus(TicketStatus.ZAPLANOWANO);

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ZAPLANOWANO");
        history.setChangedBy(manager);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto closeTicket(UUID ticketId, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User manager =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(manager.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może zamknąć zgłoszenie.");
        }

        if (ticket.getStatus() != TicketStatus.ZAKONCZONE_DO_WERYFIKACJI) {
            throw new BusinessValidationException(
                    "Zgłoszenie musi mieć status ZAKONCZONE_DO_WERYFIKACJI, aby mogło zostać"
                            + " zamknięte.");
        }

        ticket.setStatus(TicketStatus.ZAMKNIETE);
        ticket.setClosedAt(LocalDateTime.now());

        String conservatorName =
                ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getFirstName()
                                + " "
                                + ticket.getAssignedTo().getLastName()
                        : "Nieznany";
        String descriptionToPdf =
                ticket.getWorkDescription() != null
                        ? ticket.getWorkDescription()
                        : ticket.getDescription();
        WorkAcceptanceProtocolRequest pdfRequest =
                new WorkAcceptanceProtocolRequest(
                        ticket.getTicketNumber(), descriptionToPdf, conservatorName);

        try {
            byte[] pdfBytes = pdfGeneratorService.generateWorkAcceptanceProtocol(pdfRequest);
            Path dirPath = Paths.get("uploads/documents");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String fileName =
                    "protokol-"
                            + ticket.getTicketNumber()
                            + "-"
                            + System.currentTimeMillis()
                            + ".pdf";
            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, pdfBytes);

            Document document = new Document();
            document.setType("PROTOKOL");
            document.setTitle("Protokół odbioru - " + ticket.getTicketNumber());
            document.setFileUrl(filePath.toString());
            document.setTicket(ticket);
            document.setOwnerUser(manager);
            documentRepository.save(document);
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania i zapisu pliku PDF", e);
        }

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ZAMKNIETE");
        history.setChangedBy(manager);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto rejectTicket(
            UUID ticketId, TicketRejectRequest request, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User manager =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(manager.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może odrzucić zgłoszenie.");
        }

        ticket.setStatus(TicketStatus.ODRZUCONE);

        String currentNote =
                ticket.getInternalNote() != null ? ticket.getInternalNote() + "\n" : "";
        ticket.setInternalNote(currentNote + "Powód odrzucenia: " + request.getReason());

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ODRZUCONE");
        history.setChangedBy(manager);
        history.setComment(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto startWork(UUID ticketId, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User conservator =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"KONSERWATOR".equals(conservator.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko konserwator może rozpocząć prace.");
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().getId().equals(conservator.getId())) {
            throw new BusinessValidationException(
                    "Zgłoszenie nie jest przypisane do tego konserwatora.");
        }

        if (ticket.getStatus() != TicketStatus.ZAPLANOWANO) {
            throw new BusinessValidationException(
                    "Zgłoszenie musi mieć status ZAPLANOWANO, aby można było rozpocząć prace.");
        }

        ticket.setStatus(TicketStatus.W_REALIZACJI);

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("W_REALIZACJI");
        history.setChangedBy(conservator);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto suspendWork(
            UUID ticketId, TicketSuspendRequest request, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User conservator =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"KONSERWATOR".equals(conservator.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko konserwator może wstrzymać prace.");
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().getId().equals(conservator.getId())) {
            throw new BusinessValidationException(
                    "Zgłoszenie nie jest przypisane do tego konserwatora.");
        }

        if (ticket.getStatus() != TicketStatus.W_REALIZACJI) {
            throw new BusinessValidationException(
                    "Zgłoszenie musi mieć status W_REALIZACJI, aby można było je wstrzymać.");
        }

        ticket.setStatus(TicketStatus.WSTRZYMANO);

        String currentNote =
                ticket.getInternalNote() != null ? ticket.getInternalNote() + "\n" : "";
        ticket.setInternalNote(currentNote + "Wstrzymano prace: " + request.getReason());

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("WSTRZYMANO");
        history.setChangedBy(conservator);
        history.setComment(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto completeWork(
            UUID ticketId, TicketCompletionRequest request, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User conservator =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"KONSERWATOR".equals(conservator.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko konserwator może zakończyć prace.");
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().getId().equals(conservator.getId())) {
            throw new BusinessValidationException(
                    "Zgłoszenie nie jest przypisane do tego konserwatora.");
        }

        if (ticket.getStatus() != TicketStatus.W_REALIZACJI) {
            throw new BusinessValidationException(
                    "Zgłoszenie musi mieć status W_REALIZACJI, aby można było je zakończyć.");
        }

        ticket.setStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI);
        ticket.setWorkDescription(request.getWorkDescription());

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ZAKONCZONE_DO_WERYFIKACJI");
        history.setChangedBy(conservator);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        return mapToDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto changeStatus(
            UUID ticketId, TicketStatusChangeRequest request, String username) {
        Ticket ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        TicketStatus newStatus = TicketStatus.valueOf(request.getStatus());
        ticketStateMachine.validateTransition(ticket.getStatus(), newStatus);

        recordStatusChange(ticket, newStatus, user, request.getComment());

        return mapToDetail(ticketRepository.save(ticket));
    }

    private void recordStatusChange(
            Ticket ticket, TicketStatus newStatus, User changedBy, String comment) {
        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.ZAMKNIETE) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus(newStatus.name());
        history.setChangedBy(changedBy);
        history.setComment(comment);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);
    }

    private boolean isTicketVisibleForResident(
            Ticket ticket, UUID apartmentId, UUID staircaseId, UUID buildingId) {
        if (apartmentId != null
                && ticket.getApartment() != null
                && apartmentId.equals(ticket.getApartment().getId())) {
            return true;
        }
        if (staircaseId != null
                && ticket.getStaircase() != null
                && staircaseId.equals(ticket.getStaircase().getId())) {
            return true;
        }
        return buildingId != null
                && ticket.getBuilding() != null
                && buildingId.equals(ticket.getBuilding().getId());
    }

    private TicketDetailDto mapToDetail(Ticket ticket) {
        TicketDetailDto dto = new TicketDetailDto();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.setPlannedVisitAt(ticket.getPlannedVisitAt());
        dto.setInternalNote(ticket.getInternalNote());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setClosedAt(ticket.getClosedAt());

        if (ticket.getCategory() != null) {
            dto.setCategoryId(ticket.getCategory().getId());
            dto.setCategoryName(ticket.getCategory().getName());
        }
        if (ticket.getAuthor() != null) {
            dto.setAuthorId(ticket.getAuthor().getId());
            dto.setAuthorName(
                    ticket.getAuthor().getFirstName() + " " + ticket.getAuthor().getLastName());
        }
        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
            dto.setAssignedToName(
                    ticket.getAssignedTo().getFirstName()
                            + " "
                            + ticket.getAssignedTo().getLastName());
        }
        if (ticket.getApartment() != null) {
            dto.setApartmentId(ticket.getApartment().getId());
            dto.setLocationLabel(ticket.getApartment().getNumber());
        } else if (ticket.getStaircase() != null) {
            dto.setLocationLabel(ticket.getStaircase().getLabel());
        } else if (ticket.getBuilding() != null) {
            dto.setLocationLabel(ticket.getBuilding().getName());
        }
        return dto;
    }

    private TicketSummaryDto mapRawToSummary(Object[] row) {
        TicketSummaryDto dto = new TicketSummaryDto();
        dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
        dto.setTicketNumber(row[1] != null ? row[1].toString() : null);
        dto.setTitle(row[2] != null ? row[2].toString() : null);
        dto.setStatus(row[3] != null ? row[3].toString() : null);
        dto.setCategoryName(row[4] != null ? row[4].toString() : null);
        dto.setAuthorName(row[5] != null ? row[5].toString() : null);
        dto.setAssignedToName(row[6] != null ? row[6].toString() : null);
        dto.setLocationLabel(row[7] != null ? row[7].toString() : null);
        dto.setCreatedAt(row[8] != null ? ((java.sql.Timestamp) row[8]).toLocalDateTime() : null);
        dto.setClosedAt(row[9] != null ? ((java.sql.Timestamp) row[9]).toLocalDateTime() : null);
        return dto;
    }
}
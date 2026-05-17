package pl.edu.ur.blokur.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketHistory;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.models.User;
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
    private final DocumentService documentService;
    private final PdfGeneratorService pdfGeneratorService;
    private final TicketStateMachine ticketStateMachine;
    private final BusinessHoursCalculator businessHoursCalculator;
    private final PushNotificationService pushNotificationService;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketCategoryRepository ticketCategoryRepository,
            TicketNumberGenerator ticketNumberGenerator,
            TicketHistoryRepository ticketHistoryRepository,
            DocumentService documentService,
            PdfGeneratorService pdfGeneratorService,
            TicketStateMachine ticketStateMachine,
            BusinessHoursCalculator businessHoursCalculator,
            PushNotificationService pushNotificationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.documentService = documentService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.ticketStateMachine = ticketStateMachine;
        this.businessHoursCalculator = businessHoursCalculator;
        this.pushNotificationService = pushNotificationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initTicketNumberGenerator() {
        var year = LocalDate.now().getYear();
        var lastSeq = ticketRepository.findMaxSequenceForYear(year);
        ticketNumberGenerator.initYear(year, lastSeq);
    }

    @Transactional
    public TicketDetailDto create(TicketRequest request, String username) {
        var author =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (author.getUserApartments().isEmpty()) {
            throw new BusinessValidationException(
                    "Mieszkaniec nie ma przypisanego lokalu — nie można złożyć zgłoszenia");
        }

        var apartment = author.getUserApartments().get(0).getApartment();

        var category =
                ticketCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Kategoria o ID "
                                                        + request.getCategoryId()
                                                        + " nie istnieje"));

        var ticket = new Ticket();
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
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        var role = user.getRole();

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
                    .toList();
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
                    .toList();
        }

        if (user.getUserApartments().isEmpty()) {
            return List.of();
        }

        var ua = user.getUserApartments().get(0);
        var apt = ua.getApartment();
        var apartmentId = apt != null ? apt.getId() : null;
        var staircaseId =
                (apt != null && apt.getStaircase() != null) ? apt.getStaircase().getId() : null;
        var buildingId =
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
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getById(UUID ticketId, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Zgłoszenie o ID " + ticketId + " nie istnieje"));

        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        var role = user.getRole();

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

        var apt = user.getUserApartments().get(0).getApartment();
        var residentApartmentId = apt != null ? apt.getId() : null;
        var residentStaircaseId =
                (apt != null && apt.getStaircase() != null) ? apt.getStaircase().getId() : null;
        var residentBuildingId =
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

        var dto = mapToDetail(ticket);
        dto.setInternalNote(null);
        return dto;
    }

    /**
     * Przypisuje zgłoszenie do konserwatora, ustawiając planowaną datę wizyty i notatkę. Operacja
     * dostępna tylko dla zarządcy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param request dane z żądania (id konserwatora, data, notatka)
     * @param username email zalogowanego użytkownika
     * @return zaktualizowane DTO zgłoszenia
     */
    @Transactional
    public TicketDetailDto assignTicket(
            UUID ticketId, TicketAssignRequest request, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var manager =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(manager.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może przypisać konserwatora.");
        }

        var conservator =
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

        var result = mapToDetail(ticketRepository.save(ticket));
        if (ticket.getAuthor() != null) {
            pushNotificationService.send(
                    ticket.getAuthor().getId(),
                    PushNotificationService.EVENT_ZMIANA_STATUSU,
                    "Zgłoszenie zaplanowano",
                    "Twoje zgłoszenie \"" + ticket.getTitle() + "\" zostało zaplanowane.",
                    Map.of("ticketId", ticket.getId().toString(), "status", "ZAPLANOWANO"));
        }
        return result;
    }

    /**
     * Zamyka zgłoszenie będące w stanie ZAKONCZONE. Generuje protokół PDF i zapisuje jako nowy
     * Document. Operacja dostępna tylko dla zarządcy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param username email zalogowanego użytkownika
     * @return zaktualizowane DTO zgłoszenia
     */
    @Transactional
    public TicketDetailDto closeTicket(UUID ticketId, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var manager =
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

        // Generowanie PDF
        var conservatorName =
                ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getFirstName()
                                + " "
                                + ticket.getAssignedTo().getLastName()
                        : "Nieznany";
        var descriptionToPdf =
                ticket.getWorkDescription() != null
                        ? ticket.getWorkDescription()
                        : ticket.getDescription();
        var beforeImages =
                ticket.getImages().stream()
                        .filter(
                                img ->
                                        img.getImageType()
                                                == pl.edu.ur.blokur.models.TicketImageType.BEFORE)
                        .map(pl.edu.ur.blokur.models.TicketImage::getFilePath)
                        .toList();
        var afterImages =
                ticket.getImages().stream()
                        .filter(
                                img ->
                                        img.getImageType()
                                                == pl.edu.ur.blokur.models.TicketImageType.AFTER)
                        .map(pl.edu.ur.blokur.models.TicketImage::getFilePath)
                        .toList();

        var pdfRequest =
                new WorkAcceptanceProtocolRequest(
                        ticket.getTicketNumber(),
                        descriptionToPdf,
                        conservatorName,
                        beforeImages,
                        afterImages);

        byte[] pdfBytes = pdfGeneratorService.generateWorkAcceptanceProtocol(pdfRequest);
        documentService.storeGeneratedDocument(
                "PROTOKOL",
                "Protokół odbioru - " + ticket.getTicketNumber(),
                pdfBytes,
                manager,
                ticket.getApartment(),
                ticket,
                null);

        ticket.setStatus(TicketStatus.ZAMKNIETE);
        ticket.setClosedAt(LocalDateTime.now());

        var history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ZAMKNIETE");
        history.setChangedBy(manager);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        var result = mapToDetail(ticketRepository.save(ticket));
        if (ticket.getAuthor() != null) {
            var authorId = ticket.getAuthor().getId();
            pushNotificationService.send(
                    authorId,
                    PushNotificationService.EVENT_ZMIANA_STATUSU,
                    "Zgłoszenie zamknięte",
                    "Zgłoszenie \"" + ticket.getTitle() + "\" zostało zamknięte.",
                    Map.of("ticketId", ticket.getId().toString(), "status", "ZAMKNIETE"));
            pushNotificationService.send(
                    authorId,
                    PushNotificationService.EVENT_NOWY_DOKUMENT,
                    "Nowy dokument",
                    "Protokół odbioru dla zgłoszenia \"" + ticket.getTitle() + "\" jest dostępny.",
                    Map.of("ticketId", ticket.getId().toString(), "type", "PROTOKOL"));
        }
        return result;
    }

    @Transactional
    public TicketDetailDto rejectTicket(
            UUID ticketId, TicketRejectRequest request, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var manager =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(manager.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może odrzucić zgłoszenie.");
        }

        var currentNote = ticket.getInternalNote() != null ? ticket.getInternalNote() + "\n" : "";
        ticket.setInternalNote(currentNote + "Powód odrzucenia: " + request.getReason());

        ticket.setStatus(TicketStatus.ODRZUCONE);

        var history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ODRZUCONE");
        history.setChangedBy(manager);
        history.setComment(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        TicketDetailDto result = mapToDetail(ticketRepository.save(ticket));
        if (ticket.getAuthor() != null) {
            pushNotificationService.send(
                    ticket.getAuthor().getId(),
                    PushNotificationService.EVENT_ZMIANA_STATUSU,
                    "Zgłoszenie odrzucone",
                    "Twoje zgłoszenie \"" + ticket.getTitle() + "\" zostało odrzucone.",
                    Map.of("ticketId", ticket.getId().toString(), "status", "ODRZUCONE"));
        }
        return result;
    }

    @Transactional
    public TicketDetailDto startWork(UUID ticketId, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var conservator =
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

        var history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("W_REALIZACJI");
        history.setChangedBy(conservator);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        var result = mapToDetail(ticketRepository.save(ticket));
        if (ticket.getAuthor() != null) {
            pushNotificationService.send(
                    ticket.getAuthor().getId(),
                    PushNotificationService.EVENT_ZMIANA_STATUSU,
                    "Prace w toku",
                    "Prace przy zgłoszeniu \"" + ticket.getTitle() + "\" zostały rozpoczęte.",
                    Map.of("ticketId", ticket.getId().toString(), "status", "W_REALIZACJI"));
        }
        return result;
    }

    @Transactional
    public TicketDetailDto suspendWork(
            UUID ticketId, TicketSuspendRequest request, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var conservator =
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

        var currentNote = ticket.getInternalNote() != null ? ticket.getInternalNote() + "\n" : "";
        ticket.setInternalNote(currentNote + "Wstrzymano prace: " + request.getReason());

        var history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("WSTRZYMANO");
        history.setChangedBy(conservator);
        history.setComment(request.getReason());
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        var result = mapToDetail(ticketRepository.save(ticket));
        // Powiadomienie zarządców o wstrzymaniu zgłoszenia
        var managerIds = userRepository.findManagerIds();
        pushNotificationService.sendToUsers(
                managerIds,
                PushNotificationService.EVENT_WSTRZYMANIE,
                "Zgłoszenie wstrzymane",
                "Zgłoszenie \""
                        + ticket.getTitle()
                        + "\" zostało wstrzymane. Powód: "
                        + request.getReason(),
                Map.of("ticketId", ticket.getId().toString(), "status", "WSTRZYMANO"));
        return result;
    }

    @Transactional
    public TicketDetailDto completeWork(
            UUID ticketId, TicketCompletionRequest request, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var conservator =
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

        var history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus("ZAKONCZONE_DO_WERYFIKACJI");
        history.setChangedBy(conservator);
        history.setCreatedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);

        var result = mapToDetail(ticketRepository.save(ticket));
        // Powiadomienie zarządców o gotowości zgłoszenia do weryfikacji
        var managerIds = userRepository.findManagerIds();
        pushNotificationService.sendToUsers(
                managerIds,
                PushNotificationService.EVENT_ZMIANA_STATUSU,
                "Zgłoszenie do weryfikacji",
                "Zgłoszenie \"" + ticket.getTitle() + "\" oczekuje na weryfikację.",
                Map.of(
                        "ticketId",
                        ticket.getId().toString(),
                        "status",
                        "ZAKONCZONE_DO_WERYFIKACJI"));
        return result;
    }

    @Transactional
    public TicketDetailDto changeStatus(
            UUID ticketId, TicketStatusChangeRequest request, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Zgłoszenie nie istnieje"));
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if ("KONSERWATOR".equals(user.getRole())) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(user.getId())) {
                throw new BusinessValidationException(
                        "Konserwator może zmieniać status tylko własnych zgłoszeń");
            }
        } else if (!"ZARZADCA".equals(user.getRole())) {
            throw new BusinessValidationException("Brak uprawnień do zmiany statusu zgłoszenia");
        }

        var newStatus = request.getStatus();
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

        var history = new TicketHistory();
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
        var dto = new TicketDetailDto();
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
        var dto = new TicketSummaryDto();
        dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
        dto.setTicketNumber(row[1] != null ? row[1].toString() : null);
        dto.setTitle(row[2] != null ? row[2].toString() : null);
        dto.setStatus(row[3] != null ? row[3].toString() : null);
        dto.setCategoryName(row[4] != null ? row[4].toString() : null);
        dto.setAuthorName(row[5] != null ? row[5].toString() : null);
        dto.setAssignedToName(row[6] != null ? row[6].toString() : null);
        dto.setLocationLabel(row[7] != null ? row[7].toString() : null);
        dto.setCreatedAt(parseDateTime(row[8]));
        dto.setClosedAt(parseDateTime(row[9]));

        Integer slaHours = row[10] != null ? ((Number) row[10]).intValue() : null;
        if (slaHours != null && dto.getCreatedAt() != null) {
            double elapsed =
                    businessHoursCalculator.calculate(dto.getCreatedAt(), LocalDateTime.now());
            dto.setSlaBreached(elapsed > slaHours);
        }

        return dto;
    }

    private LocalDateTime parseDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        } else if (obj instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) obj;
        }
        throw new IllegalArgumentException("Nieznany format daty: " + obj.getClass());
    }
}

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
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRejectRequest;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketStatusChangeRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
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

/**
 * Serwis dostarczający logikę biznesową modułu zgłoszeń. Obsługuje tworzenie zgłoszeń przez
 * mieszkańców, pobieranie listy z filtrowaniem oraz pobieranie szczegółów z kontrolą uprawnień.
 */
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

    /**
     * Tworzy instancję serwisu z wymaganymi zależnościami.
     *
     * @param ticketRepository repozytorium zgłoszeń
     * @param userRepository repozytorium użytkowników
     * @param ticketCategoryRepository repozytorium kategorii zgłoszeń
     * @param ticketNumberGenerator generator numerów zgłoszeń
     * @param ticketHistoryRepository repozytorium historii zgłoszeń
     * @param documentRepository repozytorium dokumentów
     * @param pdfGeneratorService serwis generujący PDF
     */
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

    /**
     * Inicjalizuje generator numerów zgłoszeń na podstawie ostatniego zapisanego numeru w bazie.
     * Wywoływana automatycznie po starcie kontenera Spring.
     */
    @PostConstruct
    public void initTicketNumberGenerator() {
        int year = LocalDate.now().getYear();
        int lastSeq = ticketRepository.findMaxSequenceForYear(year);
        ticketNumberGenerator.initYear(year, lastSeq);
    }

    /**
     * Tworzy nowe zgłoszenie. Dostępne wyłącznie dla roli MIESZKANIEC. Lokal jest pobierany
     * automatycznie z pierwszego powiązania zalogowanego użytkownika.
     *
     * @param request dane nowego zgłoszenia
     * @param username email zalogowanego mieszkańca
     * @return DTO szczegółów utworzonego zgłoszenia
     * @throws NotFoundException gdy użytkownik lub kategoria nie istnieje
     * @throws BusinessValidationException gdy mieszkaniec nie ma przypisanego lokalu
     */
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

    /**
     * Pobiera listę zgłoszeń z filtrowaniem uwzględniając rolę zalogowanego użytkownika.
     *
     * <ul>
     *   <li>ZARZADCA — widzi wszystkie zgłoszenia, obsługuje pełny zakres filtrów
     *   <li>KONSERWATOR — widzi wyłącznie zgłoszenia do niego przypisane
     *   <li>MIESZKANIEC — widzi wyłącznie zgłoszenia swojego lokalu, klatki i budynku
     * </ul>
     *
     * @param username email zalogowanego użytkownika
     * @param filters parametry filtrowania
     * @return lista zgłoszeń jako DTO
     */
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

        // MIESZKANIEC — widzi zgłoszenia swojego lokalu/klatki/budynku
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

    /**
     * Pobiera szczegóły zgłoszenia z kontrolą uprawnień.
     *
     * <ul>
     *   <li>ZARZADCA — może zobaczyć dowolne zgłoszenie, w tym notatkę wewnętrzną
     *   <li>KONSERWATOR — widzi tylko przypisane do siebie, w tym notatkę wewnętrzną
     *   <li>MIESZKANIEC — widzi tylko zgłoszenia swojego lokalu/klatki/budynku, bez notatki
     * </ul>
     *
     * @param ticketId identyfikator zgłoszenia
     * @param username email zalogowanego użytkownika
     * @return DTO szczegółów zgłoszenia
     * @throws NotFoundException gdy zgłoszenie nie istnieje
     * @throws BusinessValidationException gdy użytkownik nie ma dostępu do zgłoszenia
     */
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

        // MIESZKANIEC — sprawdź czy zgłoszenie dotyczy jego lokalu/klatki/budynku
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
        recordStatusChange(ticket, TicketStatus.ZAPLANOWANO, manager, null);

        return mapToDetail(ticketRepository.save(ticket));
    }

    /**
     * Zamyka zgłoszenie będące w stanie ZAKONCZONE_DO_WERYFIKACJI. Generuje protokół PDF i zapisuje
     * jako nowy Document. Operacja dostępna tylko dla zarządcy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param username email zalogowanego użytkownika
     * @return zaktualizowane DTO zgłoszenia
     */
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

        // walidacja przejścia ZAKONCZONE_DO_WERYFIKACJI → ZAMKNIETE odbywa się w recordStatusChange

        // Generowanie PDF
        String conservatorName =
                ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getFirstName()
                                + " "
                                + ticket.getAssignedTo().getLastName()
                        : "Nieznany";
        String descriptionToPdf = ticket.getDescription();
        List<String> beforeImages =
                ticket.getImages().stream()
                        .filter(
                                img ->
                                        img.getImageType()
                                                == pl.edu.ur.blokur.models.TicketImageType.BEFORE)
                        .map(pl.edu.ur.blokur.models.TicketImage::getFilePath)
                        .toList();
        List<String> afterImages =
                ticket.getImages().stream()
                        .filter(
                                img ->
                                        img.getImageType()
                                                == pl.edu.ur.blokur.models.TicketImageType.AFTER)
                        .map(pl.edu.ur.blokur.models.TicketImage::getFilePath)
                        .toList();

        WorkAcceptanceProtocolRequest pdfRequest =
                new WorkAcceptanceProtocolRequest(
                        ticket.getTicketNumber(),
                        descriptionToPdf,
                        conservatorName,
                        beforeImages,
                        afterImages);

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

        recordStatusChange(ticket, TicketStatus.ZAMKNIETE, manager, null);

        return mapToDetail(ticketRepository.save(ticket));
    }

    /**
     * Odrzuca zgłoszenie podając powód, który trafia do historii statusów i notatki wewnętrznej.
     * Operacja dostępna tylko dla zarządcy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param request powód odrzucenia
     * @param username email zalogowanego użytkownika
     * @return zaktualizowane DTO zgłoszenia
     */
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

        String currentNote =
                ticket.getInternalNote() != null ? ticket.getInternalNote() + "\n" : "";
        ticket.setInternalNote(currentNote + "Powód odrzucenia: " + request.getReason());
        recordStatusChange(ticket, TicketStatus.ODRZUCONE, manager, request.getReason());

        return mapToDetail(ticketRepository.save(ticket));
    }

    /**
     * Zmienia status zgłoszenia z walidacją state-machine i zapisem do historii. Dostępna dla
     * KONSERWATORA (W_REALIZACJI, WSTRZYMANO, ZAKONCZONE_DO_WERYFIKACJI) i ZARZĄDCY.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param request nowy status i opcjonalny komentarz
     * @param username email zalogowanego użytkownika
     * @return zaktualizowane DTO zgłoszenia
     */
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

        if ("KONSERWATOR".equals(user.getRole())) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(user.getId())) {
                throw new BusinessValidationException(
                        "Konserwator może zmieniać status tylko własnych zgłoszeń");
            }
        } else if (!"ZARZADCA".equals(user.getRole())) {
            throw new BusinessValidationException("Brak uprawnień do zmiany statusu zgłoszenia");
        }

        recordStatusChange(ticket, request.getStatus(), user, request.getComment());
        return mapToDetail(ticketRepository.save(ticket));
    }

    /**
     * Waliduje przejście statusu przez state-machine, ustawia nowy status i zapisuje wpis do
     * ticket_history z changedBy = przekazany użytkownik.
     *
     * @param ticket encja zgłoszenia
     * @param newStatus docelowy status
     * @param changedBy użytkownik dokonujący zmiany
     * @param comment opcjonalny komentarz
     */
    private void recordStatusChange(
            Ticket ticket, TicketStatus newStatus, User changedBy, String comment) {
        ticketStateMachine.validateTransition(ticket.getStatus(), newStatus);
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
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

    /**
     * Sprawdza, czy dane zgłoszenie jest widoczne dla mieszkańca na podstawie jego hierarchii
     * lokalu.
     *
     * @param ticket zgłoszenie do sprawdzenia
     * @param apartmentId UUID lokalu mieszkańca
     * @param staircaseId UUID klatki mieszkańca
     * @param buildingId UUID budynku mieszkańca
     * @return {@code true} gdy zgłoszenie dotyczy lokalu, klatki lub budynku mieszkańca
     */
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

    /**
     * Mapuje encję {@link Ticket} na {@link TicketDetailDto}.
     *
     * @param ticket encja zgłoszenia
     * @return DTO ze szczegółami zgłoszenia
     */
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

    /**
     * Mapuje surowy wiersz z natywnego zapytania SQL na {@link TicketSummaryDto}.
     *
     * <p>Kolejność kolumn wynika ze struktury zapytań w {@link
     * pl.edu.ur.blokur.repository.TicketRepository}: id, ticket_number, title, status,
     * category_name, author_name, assigned_to_name, location_label, created_at, closed_at.
     *
     * @param row tablica obiektów z wynikiem zapytania natywnego
     * @return DTO podsumowania zgłoszenia
     */
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

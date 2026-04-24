package pl.edu.ur.blokur.service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketCategory;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.TicketCategoryRepository;
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

    /**
     * Tworzy instancję serwisu z wymaganymi zależnościami.
     *
     * @param ticketRepository repozytorium zgłoszeń
     * @param userRepository repozytorium użytkowników
     * @param ticketCategoryRepository repozytorium kategorii zgłoszeń
     * @param ticketNumberGenerator generator numerów zgłoszeń
     */
    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketCategoryRepository ticketCategoryRepository,
            TicketNumberGenerator ticketNumberGenerator) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
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

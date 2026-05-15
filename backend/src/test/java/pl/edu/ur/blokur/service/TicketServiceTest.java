package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.TicketAssignRequest;
import pl.edu.ur.blokur.dto.TicketCompletionRequest;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRejectRequest;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.dto.TicketSuspendRequest;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.Staircase;
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
 * Testy jednostkowe dla {@link TicketService}. Weryfikują logikę tworzenia zgłoszeń, pobierania
 * listy z filtrowaniem według roli oraz pobierania szczegółów z kontrolą uprawnień.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService — serwis zgłoszeń")
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private TicketCategoryRepository ticketCategoryRepository;
    @Mock private TicketNumberGenerator ticketNumberGenerator;
    @Mock private TicketHistoryRepository ticketHistoryRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private TicketStateMachine ticketStateMachine;
    @Mock private BusinessHoursCalculator businessHoursCalculator;
    @Mock private PushNotificationService pushNotificationService;

    @InjectMocks private TicketService ticketService;

    private static final String ZARZADCA_EMAIL = "zarzadca@blokur.pl";
    private static final String MIESZKANIEC_EMAIL = "mieszkaniec@blokur.pl";
    private static final String KONSERWATOR_EMAIL = "konserwator@blokur.pl";

    private UUID ticketId;
    private UUID categoryId;
    private UUID apartmentId;
    private UUID staircaseId;
    private UUID buildingId;

    private User zarzadca;
    private User mieszkaniec;
    private User konserwator;
    private TicketCategory category;
    private Apartment apartment;
    private Staircase staircase;
    private Building building;
    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();
        staircaseId = UUID.randomUUID();
        buildingId = UUID.randomUUID();

        building = new Building();
        building.setId(buildingId);
        building.setName("Budynek Testowy");

        staircase = new Staircase();
        staircase.setId(staircaseId);
        staircase.setLabel("A");
        staircase.setBuilding(building);

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("10");
        apartment.setStaircase(staircase);

        category = new TicketCategory();
        category.setId(categoryId);
        category.setName("Awaria elektryczna");

        zarzadca = buildUser(UUID.randomUUID(), ZARZADCA_EMAIL, "ZARZADCA", new ArrayList<>());

        UserApartment ua = new UserApartment();
        ua.setApartment(apartment);
        mieszkaniec =
                buildUser(
                        UUID.randomUUID(),
                        MIESZKANIEC_EMAIL,
                        "MIESZKANIEC",
                        new ArrayList<>(List.of(ua)));

        konserwator =
                buildUser(UUID.randomUUID(), KONSERWATOR_EMAIL, "KONSERWATOR", new ArrayList<>());

        sampleTicket = buildTicket("Cieknie kran", apartment, null);
        sampleTicket.setId(ticketId);
        sampleTicket.setAuthor(mieszkaniec);
    }

    // -------------------------------------------------------
    // Metody pomocnicze
    // -------------------------------------------------------

    private User buildUser(UUID id, String email, String role, List<UserApartment> apartments) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName("Jan");
        u.setLastName("Testowy");
        u.setRole(role);
        u.setUserApartments(apartments);
        return u;
    }

    private Ticket buildTicket(String title, Apartment apt, User assignedTo) {
        Ticket t = new Ticket();
        t.setId(UUID.randomUUID());
        t.setTicketNumber("ZGL-2026-0001");
        t.setTitle(title);
        t.setDescription("Opis zgłoszenia testowego");
        t.setStatus(TicketStatus.NOWE);
        t.setCategory(category);
        t.setApartment(apt);
        t.setAssignedTo(assignedTo);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    // =======================================================
    // create — tworzenie zgłoszenia
    // =======================================================

    @Nested
    @DisplayName("create — tworzenie zgłoszenia")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zapisuje zgłoszenie i zwraca DTO")
        void shouldCreateTicketAndReturnDto() {
            TicketRequest request =
                    new TicketRequest("Awaria prądu", "Brak prądu w mieszkaniu", categoryId);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(ticketNumberGenerator.generate()).thenReturn("ZGL-2026-0001");
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.create(request, MIESZKANIEC_EMAIL);

            assertThat(result.getTitle()).isEqualTo("Awaria prądu");
            assertThat(result.getTicketNumber()).isEqualTo("ZGL-2026-0001");
            assertThat(result.getStatus()).isEqualTo("NOWE");
            assertThat(result.getCategoryName()).isEqualTo("Awaria elektryczna");
        }

        @Test
        @DisplayName("Zapisuje encję z polem apartment z konta użytkownika")
        void shouldAssignApartmentFromUserAccount() {
            TicketRequest request = new TicketRequest("Awaria prądu", "Opis", categoryId);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(ticketNumberGenerator.generate()).thenReturn("ZGL-2026-0001");
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            ticketService.create(request, MIESZKANIEC_EMAIL);

            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());
            Ticket saved = captor.getValue();

            assertThat(saved.getApartment()).isEqualTo(apartment);
            assertThat(saved.getAuthor()).isEqualTo(mieszkaniec);
            assertThat(saved.getStatus()).isEqualTo(TicketStatus.NOWE);
        }

        @Test
        @DisplayName("Status domyślnie NOWE po utworzeniu")
        void shouldSetStatusToNowe() {
            TicketRequest request = new TicketRequest("Problem", "Opis", categoryId);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(ticketNumberGenerator.generate()).thenReturn("ZGL-2026-0002");
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.create(request, MIESZKANIEC_EMAIL);

            assertThat(result.getStatus()).isEqualTo(TicketStatus.NOWE.name());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowWhenUserNotFound() {
            TicketRequest request = new TicketRequest("Awaria", "Opis", categoryId);

            when(userRepository.findByEmail("nieznany@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.create(request, "nieznany@blokur.pl"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Użytkownik nie istnieje");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mieszkaniec bez lokalu — rzuca BusinessValidationException")
        void shouldThrowWhenResidentHasNoApartment() {
            mieszkaniec.setUserApartments(new ArrayList<>());
            TicketRequest request = new TicketRequest("Awaria", "Opis", categoryId);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(() -> ticketService.create(request, MIESZKANIEC_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("nie ma przypisanego lokalu");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejąca kategoria — rzuca NotFoundException")
        void shouldThrowWhenCategoryNotFound() {
            UUID unknownCategoryId = UUID.randomUUID();
            TicketRequest request = new TicketRequest("Awaria", "Opis", unknownCategoryId);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCategoryRepository.findById(unknownCategoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.create(request, MIESZKANIEC_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Kategoria o ID");

            verify(ticketRepository, never()).save(any());
        }
    }

    // =======================================================
    // getAll — lista z filtrowaniem per rola
    // =======================================================

    @Nested
    @DisplayName("getAll — lista zgłoszeń z filtrowaniem")
    class GetAllTests {

        @Test
        @DisplayName("Zarządca — wywołuje findWithFilters ze wszystkimi parametrami")
        void shouldUseFilterQueryForZarzadca() {
            TicketFilterParams filters = new TicketFilterParams();
            filters.setStatus("NOWE");

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(ticketRepository.findWithFilters("NOWE", null, null, null, null, null, null, null))
                    .thenReturn(List.of());

            List<TicketSummaryDto> result = ticketService.getAll(ZARZADCA_EMAIL, filters);

            assertThat(result).isEmpty();
            verify(ticketRepository)
                    .findWithFilters("NOWE", null, null, null, null, null, null, null);
            verify(ticketRepository, never())
                    .findForResidentWithFilters(
                            any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Konserwator — wywołuje findForConservatorWithFilters ze swoim ID")
        void shouldUseConservatorQueryForKonserwator() {
            TicketFilterParams filters = new TicketFilterParams();

            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));
            when(ticketRepository.findForConservatorWithFilters(
                            konserwator.getId(), null, null, null, null, null))
                    .thenReturn(List.of());

            List<TicketSummaryDto> result = ticketService.getAll(KONSERWATOR_EMAIL, filters);

            assertThat(result).isEmpty();
            verify(ticketRepository)
                    .findForConservatorWithFilters(
                            konserwator.getId(), null, null, null, null, null);
        }

        @Test
        @DisplayName("Mieszkaniec — wywołuje findForResidentWithFilters z ID lokalu/klatki/budynku")
        void shouldUseResidentQueryForMieszkaniec() {
            TicketFilterParams filters = new TicketFilterParams();

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketRepository.findForResidentWithFilters(
                            apartmentId, staircaseId, buildingId, null, null, null, null, null))
                    .thenReturn(List.of());

            List<TicketSummaryDto> result = ticketService.getAll(MIESZKANIEC_EMAIL, filters);

            assertThat(result).isEmpty();
            verify(ticketRepository)
                    .findForResidentWithFilters(
                            apartmentId, staircaseId, buildingId, null, null, null, null, null);
        }

        @Test
        @DisplayName("Mieszkaniec bez lokalu — zwraca pustą listę bez wywołania repozytorium")
        void shouldReturnEmptyListWhenResidentHasNoApartment() {
            mieszkaniec.setUserApartments(new ArrayList<>());
            TicketFilterParams filters = new TicketFilterParams();

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            List<TicketSummaryDto> result = ticketService.getAll(MIESZKANIEC_EMAIL, filters);

            assertThat(result).isEmpty();
            verify(ticketRepository, never())
                    .findForResidentWithFilters(
                            any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail("nieznany@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    ticketService.getAll(
                                            "nieznany@blokur.pl", new TicketFilterParams()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Użytkownik nie istnieje");
        }
    }

    // =======================================================
    // getById — szczegóły z kontrolą uprawnień
    // =======================================================

    @Nested
    @DisplayName("getById — szczegóły zgłoszenia z kontrolą uprawnień")
    class GetByIdTests {

        @Test
        @DisplayName("Zarządca — może zobaczyć dowolne zgłoszenie z notatką wewnętrzną")
        void shouldReturnTicketWithInternalNoteForZarzadca() {
            sampleTicket.setInternalNote("Notatka wewnętrzna");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            TicketDetailDto result = ticketService.getById(ticketId, ZARZADCA_EMAIL);

            assertThat(result.getInternalNote()).isEqualTo("Notatka wewnętrzna");
        }

        @Test
        @DisplayName("Konserwator — może zobaczyć przypisane zgłoszenie z notatką wewnętrzną")
        void shouldReturnTicketWithInternalNoteForAssignedKonserwator() {
            sampleTicket.setAssignedTo(konserwator);
            sampleTicket.setInternalNote("Notatka do konserwatora");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));

            TicketDetailDto result = ticketService.getById(ticketId, KONSERWATOR_EMAIL);

            assertThat(result.getInternalNote()).isEqualTo("Notatka do konserwatora");
        }

        @Test
        @DisplayName("Konserwator — nie może zobaczyć cudzego zgłoszenia")
        void shouldThrowWhenKonserwatorAccessesUnassignedTicket() {
            // ticket nie jest przypisany do konserwatora
            sampleTicket.setAssignedTo(null);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));

            assertThatThrownBy(() -> ticketService.getById(ticketId, KONSERWATOR_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("nie jest przypisane");
        }

        @Test
        @DisplayName("Mieszkaniec — widzi zgłoszenie swojego lokalu bez notatki wewnętrznej")
        void shouldReturnTicketWithoutInternalNoteForResident() {
            sampleTicket.setInternalNote("Tajne informacje");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            TicketDetailDto result = ticketService.getById(ticketId, MIESZKANIEC_EMAIL);

            assertThat(result.getInternalNote()).isNull();
            assertThat(result.getTitle()).isEqualTo("Cieknie kran");
        }

        @Test
        @DisplayName("Mieszkaniec — nie może zobaczyć zgłoszenia obcego lokalu")
        void shouldThrowWhenResidentAccessesForeignTicket() {
            Apartment otherApartment = new Apartment();
            otherApartment.setId(UUID.randomUUID());
            otherApartment.setNumber("99");
            otherApartment.setStaircase(staircase);

            sampleTicket.setApartment(otherApartment);
            sampleTicket.setStaircase(null);
            sampleTicket.setBuilding(null);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(() -> ticketService.getById(ticketId, MIESZKANIEC_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Brak dostępu");
        }

        @Test
        @DisplayName("Nieistniejące zgłoszenie — rzuca NotFoundException")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getById(ticketId, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Zgłoszenie o ID");
        }

        @Test
        @DisplayName("Mieszkaniec bez lokalu — rzuca BusinessValidationException")
        void shouldThrowWhenResidentHasNoApartment() {
            mieszkaniec.setUserApartments(new ArrayList<>());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(() -> ticketService.getById(ticketId, MIESZKANIEC_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("nie ma przypisanego lokalu");
        }

        @Test
        @DisplayName("Mieszkaniec — widzi zgłoszenie swojej klatki schodowej")
        void shouldAllowResidentToViewStaircaseTicket() {
            Ticket staircaseTicket = buildTicket("Problem w klatce", null, null);
            staircaseTicket.setId(ticketId);
            staircaseTicket.setStaircase(staircase);
            staircaseTicket.setApartment(null);
            staircaseTicket.setBuilding(null);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(staircaseTicket));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            TicketDetailDto result = ticketService.getById(ticketId, MIESZKANIEC_EMAIL);

            assertThat(result.getTitle()).isEqualTo("Problem w klatce");
        }

        @Test
        @DisplayName("Mieszkaniec — widzi zgłoszenie swojego budynku")
        void shouldAllowResidentToViewBuildingTicket() {
            Ticket buildingTicket = buildTicket("Awaria windy", null, null);
            buildingTicket.setId(ticketId);
            buildingTicket.setBuilding(building);
            buildingTicket.setApartment(null);
            buildingTicket.setStaircase(null);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(buildingTicket));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            TicketDetailDto result = ticketService.getById(ticketId, MIESZKANIEC_EMAIL);

            assertThat(result.getTitle()).isEqualTo("Awaria windy");
        }
    }

    // =======================================================
    // Mapowanie na DTO
    // =======================================================

    @Nested
    @DisplayName("Mapowanie encji na DTO")
    class MappingTests {

        @Test
        @DisplayName("Mapuje wszystkie podstawowe pola na TicketDetailDto")
        void shouldMapAllFieldsToDetailDto() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 24, 10, 0);
            sampleTicket.setCreatedAt(now);
            sampleTicket.setPlannedVisitAt(now.plusDays(3));
            sampleTicket.setInternalNote("Notatka");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            TicketDetailDto result = ticketService.getById(ticketId, ZARZADCA_EMAIL);

            assertThat(result.getId()).isEqualTo(sampleTicket.getId());
            assertThat(result.getTicketNumber()).isEqualTo("ZGL-2026-0001");
            assertThat(result.getTitle()).isEqualTo("Cieknie kran");
            assertThat(result.getStatus()).isEqualTo("NOWE");
            assertThat(result.getCategoryName()).isEqualTo("Awaria elektryczna");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getPlannedVisitAt()).isEqualTo(now.plusDays(3));
            assertThat(result.getInternalNote()).isEqualTo("Notatka");
            assertThat(result.getLocationLabel()).isEqualTo("10");
        }

        @Test
        @DisplayName("Zgłoszenie do klatki — locationLabel to etykieta klatki")
        void shouldSetLocationLabelFromStaircase() {
            Ticket staircaseTicket = buildTicket("Usterka klatki", null, null);
            staircaseTicket.setId(UUID.randomUUID());
            staircaseTicket.setStaircase(staircase);
            staircaseTicket.setApartment(null);

            when(ticketRepository.findById(staircaseTicket.getId()))
                    .thenReturn(Optional.of(staircaseTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            TicketDetailDto result = ticketService.getById(staircaseTicket.getId(), ZARZADCA_EMAIL);

            assertThat(result.getLocationLabel()).isEqualTo("A");
        }

        @Test
        @DisplayName("Zgłoszenie do budynku — locationLabel to nazwa budynku")
        void shouldSetLocationLabelFromBuilding() {
            Ticket buildingTicket = buildTicket("Awaria dźwigu", null, null);
            buildingTicket.setId(UUID.randomUUID());
            buildingTicket.setBuilding(building);
            buildingTicket.setApartment(null);
            buildingTicket.setStaircase(null);

            when(ticketRepository.findById(buildingTicket.getId()))
                    .thenReturn(Optional.of(buildingTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            TicketDetailDto result = ticketService.getById(buildingTicket.getId(), ZARZADCA_EMAIL);

            assertThat(result.getLocationLabel()).isEqualTo("Budynek Testowy");
        }
    }

    // =======================================================
    // Operacje zarządcy
    // =======================================================

    @Nested
    @DisplayName("Operacje zarzadcy")
    class ManagerOperationsTests {

        @Test
        @DisplayName("assignTicket — przypisuje konserwatora")
        void shouldAssignTicket() {
            TicketAssignRequest request = new TicketAssignRequest();
            request.setAssignedTo(konserwator.getId());
            request.setPlannedVisitAt(LocalDateTime.now().plusDays(1));

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(userRepository.findById(konserwator.getId())).thenReturn(Optional.of(konserwator));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.assignTicket(ticketId, request, ZARZADCA_EMAIL);

            assertThat(result.getStatus()).isEqualTo("ZAPLANOWANO");
            assertThat(result.getAssignedToId()).isEqualTo(konserwator.getId());
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("closeTicket — zamyka zgloszenie i generuje PDF")
        void shouldCloseTicket() {
            sampleTicket.setStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(pdfGeneratorService.generateWorkAcceptanceProtocol(any()))
                    .thenReturn(new byte[] {1, 2, 3});
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.closeTicket(ticketId, ZARZADCA_EMAIL);

            assertThat(result.getStatus()).isEqualTo("ZAMKNIETE");
            verify(pdfGeneratorService)
                    .generateWorkAcceptanceProtocol(any(WorkAcceptanceProtocolRequest.class));
            verify(documentRepository).save(any(Document.class));
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("rejectTicket — odrzuca zgloszenie")
        void shouldRejectTicket() {
            TicketRejectRequest request = new TicketRejectRequest();
            request.setReason("Brak podstaw");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.rejectTicket(ticketId, request, ZARZADCA_EMAIL);

            assertThat(result.getStatus()).isEqualTo("ODRZUCONE");
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }
    }

    // =======================================================
    // SLA — slaBreached w odpowiedzi getAll
    // =======================================================

    @Nested
    @DisplayName("SLA — pole slaBreached na liście zgłoszeń")
    class SlaBreachedTests {

        private Object[] buildRawRow(LocalDateTime createdAt, Integer slaHours) {
            return new Object[] {
                UUID.randomUUID().toString(),
                "ZGL-2026-0001",
                "Tytuł testowy",
                "NOWE",
                "Hydraulika",
                "Jan Testowy",
                null,
                "10",
                createdAt != null ? Timestamp.valueOf(createdAt) : null,
                null,
                slaHours
            };
        }

        @Test
        @DisplayName("slaBreached = true gdy elapsed > slaHours")
        void setsSlaBreahedTrueWhenElapsedExceedsSla() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 20, 8, 0);
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(ticketRepository.findWithFilters(null, null, null, null, null, null, null, null))
                    .thenReturn(List.<Object[]>of(buildRawRow(createdAt, 4)));
            when(businessHoursCalculator.calculate(
                            any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(8.0);

            List<TicketSummaryDto> result =
                    ticketService.getAll(ZARZADCA_EMAIL, new TicketFilterParams());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isSlaBreached()).isTrue();
        }

        @Test
        @DisplayName("slaBreached = false gdy elapsed <= slaHours")
        void setsSlaBreahedFalseWhenElapsedWithinSla() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 20, 8, 0);
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(ticketRepository.findWithFilters(null, null, null, null, null, null, null, null))
                    .thenReturn(List.<Object[]>of(buildRawRow(createdAt, 24)));
            when(businessHoursCalculator.calculate(
                            any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(8.0);

            List<TicketSummaryDto> result =
                    ticketService.getAll(ZARZADCA_EMAIL, new TicketFilterParams());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isSlaBreached()).isFalse();
        }

        @Test
        @DisplayName("slaBreached = false gdy sla_hours null (brak konfiguracji SLA)")
        void setsSlaBreahedFalseWhenSlaHoursNull() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 20, 8, 0);
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(ticketRepository.findWithFilters(null, null, null, null, null, null, null, null))
                    .thenReturn(List.<Object[]>of(buildRawRow(createdAt, null)));

            List<TicketSummaryDto> result =
                    ticketService.getAll(ZARZADCA_EMAIL, new TicketFilterParams());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isSlaBreached()).isFalse();
        }
    }

    // =======================================================
    // Operacje Konserwatora
    // =======================================================

    @Nested
    @DisplayName("Operacje konserwatora")
    class ConservatorOperationsTests {

        @Test
        @DisplayName("startWork — rozpoczyna prace nad zgłoszeniem")
        void shouldStartWork() {
            sampleTicket.setAssignedTo(konserwator);
            sampleTicket.setStatus(TicketStatus.ZAPLANOWANO);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result = ticketService.startWork(ticketId, KONSERWATOR_EMAIL);

            assertThat(result.getStatus()).isEqualTo("W_REALIZACJI");
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("startWork — rzuca błąd gdy nie jest przypisane do tego konserwatora")
        void shouldThrowWhenStartWorkNotAssigned() {
            sampleTicket.setAssignedTo(null);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));

            assertThatThrownBy(() -> ticketService.startWork(ticketId, KONSERWATOR_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("nie jest przypisane");
        }

        @Test
        @DisplayName("suspendWork — wstrzymuje prace i zapisuje notatkę")
        void shouldSuspendWork() {
            sampleTicket.setAssignedTo(konserwator);
            sampleTicket.setStatus(TicketStatus.W_REALIZACJI);
            TicketSuspendRequest request = new TicketSuspendRequest("Brak czesci");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result =
                    ticketService.suspendWork(ticketId, request, KONSERWATOR_EMAIL);

            assertThat(result.getStatus()).isEqualTo("WSTRZYMANO");
            assertThat(sampleTicket.getInternalNote()).contains("Brak czesci");
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }

        @Test
        @DisplayName("completeWork — kończy prace i zapisuje opis")
        void shouldCompleteWork() {
            sampleTicket.setAssignedTo(konserwator);
            sampleTicket.setStatus(TicketStatus.W_REALIZACJI);
            TicketCompletionRequest request = new TicketCompletionRequest("Wymieniono uszczelkę");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(sampleTicket));
            when(userRepository.findByEmail(KONSERWATOR_EMAIL))
                    .thenReturn(Optional.of(konserwator));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

            TicketDetailDto result =
                    ticketService.completeWork(ticketId, request, KONSERWATOR_EMAIL);

            assertThat(result.getStatus()).isEqualTo("ZAKONCZONE_DO_WERYFIKACJI");
            assertThat(sampleTicket.getWorkDescription()).isEqualTo("Wymieniono uszczelkę");
            verify(ticketHistoryRepository).save(any(TicketHistory.class));
        }
    }
}

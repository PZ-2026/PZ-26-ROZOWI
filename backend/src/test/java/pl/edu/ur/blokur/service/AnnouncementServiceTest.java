package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.dto.AnnouncementRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Announcement;
import pl.edu.ur.blokur.models.AnnouncementTargetType;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.AnnouncementRepository;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.StaircaseRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Testy jednostkowe dla {@link AnnouncementService}. Weryfikują operacje CRUD, walidację uprawnień
 * i filtrowanie ogłoszeń.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnnouncementService — serwis ogłoszeń")
class AnnouncementServiceTest {

    @Mock private AnnouncementRepository announcementRepository;
    @Mock private UserRepository userRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private StaircaseRepository staircaseRepository;
    @Mock private ApartmentRepository apartmentRepository;

    @InjectMocks private AnnouncementService announcementService;

    private static final String EMAIL = "mieszkaniec@blokur.pl";
    private static final String ZARZADCA_EMAIL = "zarzadca@blokur.pl";

    private UUID buildingId;
    private UUID staircaseId;
    private UUID apartmentId;
    private User user;
    private User zarzadca;
    private Building building;
    private Staircase staircase;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        staircaseId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();

        building = new Building();
        building.setId(buildingId);
        building.setEstateName("Osiedle Testowe");
        building.setName("Blok 1");
        building.setAddress("ul. Testowa 1, 00-001 Warszawa");

        staircase = new Staircase();
        staircase.setId(staircaseId);
        staircase.setLabel("Klatka A");
        staircase.setBuilding(building);

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("1");
        apartment.setStaircase(staircase);

        UserApartment ua = new UserApartment();
        ua.setApartment(apartment);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setFirstName("Jan");
        user.setLastName("Testowy");
        user.setRole("MIESZKANIEC");
        user.setUserApartments(new ArrayList<>(List.of(ua)));

        zarzadca = new User();
        zarzadca.setId(UUID.randomUUID());
        zarzadca.setEmail(ZARZADCA_EMAIL);
        zarzadca.setFirstName("Anna");
        zarzadca.setLastName("Zarzadca");
        zarzadca.setRole("ZARZADCA");
    }

    // =======================================================
    // Pomocnicza metoda budująca ogłoszenie testowe
    // =======================================================

    private Announcement buildAnnouncement(String title, String content) {
        User author = new User();
        author.setFirstName("Admin");
        author.setLastName("Testowy");

        Announcement ann = new Announcement();
        ann.setId(UUID.randomUUID());
        ann.setType("OGLOSZENIE");
        ann.setTitle(title);
        ann.setContent(content);
        ann.setAuthor(author);
        ann.setTargetType(AnnouncementTargetType.WSZYSCY);
        ann.setCreatedAt(LocalDateTime.now());
        return ann;
    }

    // =======================================================
    // Pobieranie ogłoszeń — scenariusze użytkownika z lokalem
    // =======================================================

    @Nested
    @DisplayName("Użytkownik przypisany do lokalu")
    class UserWithApartmentTests {

        @Test
        @DisplayName("Zwraca ogłoszenia dopasowane do hierarchii użytkownika")
        void shouldReturnAnnouncementsForUserWithApartment() {
            List<Announcement> announcements =
                    List.of(
                            buildAnnouncement("Przegląd kominiarski", "Treść ogłoszenia 1"),
                            buildAnnouncement("Przerwa w wodzie", "Treść ogłoszenia 2"));

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(
                            eq(buildingId), eq(staircaseId), eq(apartmentId), any()))
                    .thenReturn(announcements);

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Przegląd kominiarski");
            assertThat(result.get(1).getTitle()).isEqualTo("Przerwa w wodzie");
        }

        @Test
        @DisplayName("Wywołuje repozytorium z poprawnymi ID budynku, klatki i lokalu")
        void shouldPassCorrectIdsToRepository() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(
                            eq(buildingId), eq(staircaseId), eq(apartmentId), any()))
                    .thenReturn(List.of());

            announcementService.getAnnouncementsForUser(EMAIL);

            verify(announcementRepository)
                    .findForUserAfterDate(eq(buildingId), eq(staircaseId), eq(apartmentId), any());
        }

        @Test
        @DisplayName("Pusta lista ogłoszeń z repozytorium — zwraca pustą listę DTO")
        void shouldReturnEmptyListWhenNoAnnouncementsFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(any(), any(), any(), any()))
                    .thenReturn(List.of());

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).isEmpty();
        }
    }

    // =======================================================
    // Pobieranie ogłoszeń — użytkownik bez lokalu
    // =======================================================

    @Nested
    @DisplayName("Użytkownik bez przypisanego lokalu")
    class UserWithoutApartmentTests {

        @Test
        @DisplayName("Użytkownik bez lokali — wywołuje repozytorium z nullami")
        void shouldPassNullIdsWhenUserHasNoApartment() {
            user.setUserApartments(new ArrayList<>());

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(isNull(), isNull(), isNull(), any()))
                    .thenReturn(List.of());

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).isEmpty();
            verify(announcementRepository)
                    .findForUserAfterDate(isNull(), isNull(), isNull(), any());
        }
    }

    // =======================================================
    // Pobieranie ogłoszeń — nieznany użytkownik
    // =======================================================

    @Nested
    @DisplayName("Nieistniejący użytkownik")
    class UnknownUserTests {

        @Test
        @DisplayName("Brak użytkownika w bazie — wywołuje repozytorium z nullami")
        void shouldPassNullIdsWhenUserNotFound() {
            when(userRepository.findByEmail("nieznany@blokur.pl")).thenReturn(Optional.empty());
            when(announcementRepository.findForUserAfterDate(isNull(), isNull(), isNull(), any()))
                    .thenReturn(List.of());

            List<AnnouncementDto> result =
                    announcementService.getAnnouncementsForUser("nieznany@blokur.pl");

            assertThat(result).isEmpty();
            verify(announcementRepository)
                    .findForUserAfterDate(isNull(), isNull(), isNull(), any());
        }
    }

    // =======================================================
    // Mapowanie ogłoszenia na DTO
    // =======================================================

    @Nested
    @DisplayName("Mapowanie encji na DTO")
    class MappingTests {

        @Test
        @DisplayName("Wszystkie pola ogłoszenia są poprawnie mapowane na DTO")
        void shouldMapAnnouncementFieldsToDto() {
            Announcement ann = buildAnnouncement("Zebranie wspólnoty", "Zapraszamy 30.04.");
            ann.setPlannedDate(LocalDateTime.of(2026, 4, 30, 18, 0));

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(any(), any(), any(), any()))
                    .thenReturn(List.of(ann));

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).hasSize(1);
            AnnouncementDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(ann.getId());
            assertThat(dto.getType()).isEqualTo("OGLOSZENIE");
            assertThat(dto.getTitle()).isEqualTo("Zebranie wspólnoty");
            assertThat(dto.getContent()).isEqualTo("Zapraszamy 30.04.");
            assertThat(dto.getAuthorName()).isEqualTo("Admin Testowy");
            assertThat(dto.getTargetType()).isEqualTo("WSZYSCY");
            assertThat(dto.getPlannedDate()).isEqualTo(LocalDateTime.of(2026, 4, 30, 18, 0));
        }

        @Test
        @DisplayName("Ogłoszenie bez autora — authorName jest pustym Stringiem")
        void shouldReturnEmptyAuthorNameWhenAuthorIsNull() {
            Announcement ann = buildAnnouncement("Bez autora", "Treść");
            ann.setAuthor(null);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(any(), any(), any(), any()))
                    .thenReturn(List.of(ann));

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result.get(0).getAuthorName()).isEmpty();
        }

        @Test
        @DisplayName("Ogłoszenie bez plannedDate — pole jest null w DTO")
        void shouldReturnNullPlannedDateWhenNotSet() {
            Announcement ann = buildAnnouncement("Bez daty", "Treść");
            ann.setPlannedDate(null);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUserAfterDate(any(), any(), any(), any()))
                    .thenReturn(List.of(ann));

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result.get(0).getPlannedDate()).isNull();
        }
    }

    // =======================================================
    // Tworzenie ogłoszenia
    // =======================================================

    @Nested
    @DisplayName("Tworzenie ogłoszenia")
    class CreateAnnouncementTests {

        @Test
        @DisplayName("Zarządca tworzy ogłoszenie globalne")
        void shouldCreateGlobalAnnouncement() {
            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Nowe ogłoszenie");
            request.setContent("<p>Treść ogłoszenia</p>");
            request.setTargetType(AnnouncementTargetType.WSZYSCY);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(announcementRepository.save(any(Announcement.class)))
                    .thenAnswer(
                            inv -> {
                                Announcement a = inv.getArgument(0);
                                a.setId(UUID.randomUUID());
                                a.setCreatedAt(LocalDateTime.now());
                                return a;
                            });

            AnnouncementDto result =
                    announcementService.createAnnouncement(request, null, ZARZADCA_EMAIL);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Nowe ogłoszenie");
            assertThat(result.getTargetType()).isEqualTo("WSZYSCY");
            verify(announcementRepository).save(any(Announcement.class));
        }

        @Test
        @DisplayName("Zarządca tworzy ogłoszenie dla budynku")
        void shouldCreateBuildingAnnouncement() {
            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Ogłoszenie dla budynku");
            request.setContent("Treść");
            request.setTargetType(AnnouncementTargetType.BUDYNEK);
            request.setTargetId(buildingId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(announcementRepository.save(any(Announcement.class)))
                    .thenAnswer(
                            inv -> {
                                Announcement a = inv.getArgument(0);
                                a.setId(UUID.randomUUID());
                                a.setCreatedAt(LocalDateTime.now());
                                return a;
                            });

            AnnouncementDto result =
                    announcementService.createAnnouncement(request, null, ZARZADCA_EMAIL);

            assertThat(result).isNotNull();
            assertThat(result.getTargetType()).isEqualTo("BUDYNEK");
        }

        @Test
        @DisplayName("Mieszkaniec nie może tworzyć ogłoszeń")
        void shouldThrowWhenResidentTriesToCreate() {
            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Test");
            request.setContent("Treść");
            request.setTargetType(AnnouncementTargetType.WSZYSCY);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> announcementService.createAnnouncement(request, null, EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Tylko zarządca może tworzyć ogłoszenia");
        }

        @Test
        @DisplayName("Typ BUDYNEK bez targetId — rzuca wyjątek")
        void shouldThrowWhenBuildingTargetIdMissing() {
            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Test");
            request.setContent("Treść");
            request.setTargetType(AnnouncementTargetType.BUDYNEK);
            request.setTargetId(null);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            assertThatThrownBy(
                            () ->
                                    announcementService.createAnnouncement(
                                            request, null, ZARZADCA_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Wymagany identyfikator budynku");
        }

        @Test
        @DisplayName("Załącznik nie-PDF — rzuca wyjątek")
        void shouldThrowWhenAttachmentIsNotPdf() {
            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Test");
            request.setContent("Treść");
            request.setTargetType(AnnouncementTargetType.WSZYSCY);

            MockMultipartFile file =
                    new MockMultipartFile(
                            "attachment", "test.jpg", "image/jpeg", new byte[] {1, 2, 3});

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            assertThatThrownBy(
                            () ->
                                    announcementService.createAnnouncement(
                                            request, file, ZARZADCA_EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Załącznik musi być plikiem PDF");
        }
    }

    // =======================================================
    // Usuwanie ogłoszenia
    // =======================================================

    @Nested
    @DisplayName("Usuwanie ogłoszenia")
    class DeleteAnnouncementTests {

        @Test
        @DisplayName("Zarządca usuwa ogłoszenie")
        void shouldDeleteAnnouncement() {
            Announcement ann = buildAnnouncement("Do usunięcia", "Treść");

            when(announcementRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));

            announcementService.deleteAnnouncement(ann.getId(), ZARZADCA_EMAIL);

            verify(announcementRepository).delete(ann);
        }

        @Test
        @DisplayName("Mieszkaniec nie może usuwać ogłoszeń")
        void shouldThrowWhenResidentTriesToDelete() {
            Announcement ann = buildAnnouncement("Test", "Treść");

            when(announcementRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> announcementService.deleteAnnouncement(ann.getId(), EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Tylko zarządca może usuwać ogłoszenia");
        }

        @Test
        @DisplayName("Nieistniejące ogłoszenie — rzuca NotFoundException")
        void shouldThrowWhenAnnouncementNotFound() {
            UUID fakeId = UUID.randomUUID();
            when(announcementRepository.findById(fakeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> announcementService.deleteAnnouncement(fakeId, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Ogłoszenie nie istnieje");
        }
    }

    // =======================================================
    // Edycja ogłoszenia
    // =======================================================

    @Nested
    @DisplayName("Edycja ogłoszenia")
    class UpdateAnnouncementTests {

        @Test
        @DisplayName("Zarządca edytuje ogłoszenie")
        void shouldUpdateAnnouncement() {
            Announcement ann = buildAnnouncement("Stary tytuł", "Stara treść");

            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Nowy tytuł");
            request.setContent("Nowa treść");
            request.setTargetType(AnnouncementTargetType.WSZYSCY);

            when(announcementRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(announcementRepository.save(any(Announcement.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AnnouncementDto result =
                    announcementService.updateAnnouncement(
                            ann.getId(), request, null, ZARZADCA_EMAIL);

            assertThat(result.getTitle()).isEqualTo("Nowy tytuł");
            assertThat(result.getContent()).isEqualTo("Nowa treść");
        }

        @Test
        @DisplayName("Mieszkaniec nie może edytować ogłoszeń")
        void shouldThrowWhenResidentTriesToUpdate() {
            Announcement ann = buildAnnouncement("Test", "Treść");

            AnnouncementRequest request = new AnnouncementRequest();
            request.setTitle("Nowy tytuł");
            request.setContent("Nowa treść");
            request.setTargetType(AnnouncementTargetType.WSZYSCY);

            when(announcementRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(
                            () ->
                                    announcementService.updateAnnouncement(
                                            ann.getId(), request, null, EMAIL))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Tylko zarządca może edytować ogłoszenia");
        }
    }
}

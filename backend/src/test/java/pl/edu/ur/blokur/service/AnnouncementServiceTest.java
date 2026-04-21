package pl.edu.ur.blokur.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.models.Announcement;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.AnnouncementRepository;
import pl.edu.ur.blokur.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe dla {@link AnnouncementService}.
 * Weryfikują logikę filtrowania ogłoszeń na podstawie hierarchii lokalizacyjnej
 * zalogowanego użytkownika (budynek → klatka → lokal).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnnouncementService — serwis ogłoszeń")
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    private static final String EMAIL = "mieszkaniec@blokur.pl";

    private UUID buildingId;
    private UUID staircaseId;
    private UUID apartmentId;
    private User user;
    private Building building;
    private Staircase staircase;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        buildingId  = UUID.randomUUID();
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
            List<Announcement> announcements = List.of(
                buildAnnouncement("Przegląd kominiarski", "Treść ogłoszenia 1"),
                buildAnnouncement("Przerwa w wodzie",     "Treść ogłoszenia 2")
            );

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUser(
                eq(buildingId), eq(staircaseId), eq(apartmentId)
            )).thenReturn(announcements);

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Przegląd kominiarski");
            assertThat(result.get(1).getTitle()).isEqualTo("Przerwa w wodzie");
        }

        @Test
        @DisplayName("Wywołuje repozytorium z poprawnymi ID budynku, klatki i lokalu")
        void shouldPassCorrectIdsToRepository() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUser(
                eq(buildingId), eq(staircaseId), eq(apartmentId)
            )).thenReturn(List.of());

            announcementService.getAnnouncementsForUser(EMAIL);

            verify(announcementRepository).findForUser(buildingId, staircaseId, apartmentId);
        }

        @Test
        @DisplayName("Pusta lista ogłoszeń z repozytorium — zwraca pustą listę DTO")
        void shouldReturnEmptyListWhenNoAnnouncementsFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUser(any(), any(), any())).thenReturn(List.of());

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
            when(announcementRepository.findForUser(isNull(), isNull(), isNull()))
                .thenReturn(List.of());

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).isEmpty();
            verify(announcementRepository).findForUser(null, null, null);
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
            when(announcementRepository.findForUser(isNull(), isNull(), isNull()))
                .thenReturn(List.of());

            List<AnnouncementDto> result =
                announcementService.getAnnouncementsForUser("nieznany@blokur.pl");

            assertThat(result).isEmpty();
            verify(announcementRepository).findForUser(null, null, null);
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
            when(announcementRepository.findForUser(any(), any(), any()))
                .thenReturn(List.of(ann));

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result).hasSize(1);
            AnnouncementDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(ann.getId());
            assertThat(dto.getType()).isEqualTo("OGLOSZENIE");
            assertThat(dto.getTitle()).isEqualTo("Zebranie wspólnoty");
            assertThat(dto.getContent()).isEqualTo("Zapraszamy 30.04.");
            assertThat(dto.getAuthorName()).isEqualTo("Admin Testowy");
            assertThat(dto.getPlannedDate()).isEqualTo(LocalDateTime.of(2026, 4, 30, 18, 0));
        }

        @Test
        @DisplayName("Ogłoszenie bez autora — authorName jest pustym Stringiem")
        void shouldReturnEmptyAuthorNameWhenAuthorIsNull() {
            Announcement ann = buildAnnouncement("Bez autora", "Treść");
            ann.setAuthor(null);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(announcementRepository.findForUser(any(), any(), any()))
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
            when(announcementRepository.findForUser(any(), any(), any()))
                .thenReturn(List.of(ann));

            List<AnnouncementDto> result = announcementService.getAnnouncementsForUser(EMAIL);

            assertThat(result.get(0).getPlannedDate()).isNull();
        }
    }
}

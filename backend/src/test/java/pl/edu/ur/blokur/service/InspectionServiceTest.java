package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.InspectionRequest;
import pl.edu.ur.blokur.dto.InspectionResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Inspection;
import pl.edu.ur.blokur.models.Property;
import pl.edu.ur.blokur.models.ScopeType;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.InspectionRepository;
import pl.edu.ur.blokur.repository.PropertyRepository;
import pl.edu.ur.blokur.repository.StaircaseRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Testy jednostkowe dla {@link InspectionService}. Weryfikują logikę tworzenia, pobierania,
 * aktualizacji i usuwania przeglądów technicznych, w tym filtrowanie po zakresie użytkownika.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InspectionService — serwis przeglądów technicznych")
class InspectionServiceTest {

    @Mock private InspectionRepository inspectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private StaircaseRepository staircaseRepository;

    @InjectMocks private InspectionService inspectionService;

    private static final String ZARZADCA_EMAIL = "zarzadca@blokur.pl";
    private static final String MIESZKANIEC_EMAIL = "mieszkaniec@blokur.pl";

    private UUID inspectionId;
    private UUID propertyId;
    private UUID buildingId;
    private UUID staircaseId;

    private User zarzadca;
    private User mieszkaniec;
    private Property property;
    private Building building;
    private Staircase staircase;
    private Inspection sampleInspection;

    @BeforeEach
    void setUp() {
        inspectionId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        buildingId = UUID.randomUUID();
        staircaseId = UUID.randomUUID();

        zarzadca = new User();
        zarzadca.setId(UUID.randomUUID());
        zarzadca.setEmail(ZARZADCA_EMAIL);
        zarzadca.setFirstName("Adam");
        zarzadca.setLastName("Zarządca");
        zarzadca.setRole("ZARZADCA");
        zarzadca.setUserApartments(new ArrayList<>());

        property = new Property();
        property.setId(propertyId);
        property.setName("Wspólnota Testowa");

        building = new Building();
        building.setId(buildingId);
        building.setProperty(property);

        staircase = new Staircase();
        staircase.setId(staircaseId);
        staircase.setBuilding(building);

        Apartment apartment = new Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setStaircase(staircase);

        UserApartment ua = new UserApartment();
        ua.setApartment(apartment);

        mieszkaniec = new User();
        mieszkaniec.setId(UUID.randomUUID());
        mieszkaniec.setEmail(MIESZKANIEC_EMAIL);
        mieszkaniec.setFirstName("Jan");
        mieszkaniec.setLastName("Mieszkaniec");
        mieszkaniec.setRole("MIESZKANIEC");
        mieszkaniec.setUserApartments(new ArrayList<>(List.of(ua)));

        sampleInspection = buildInspection("Przegląd gazowy", ScopeType.BUDYNEK, buildingId);
    }

    // =======================================================
    // Pomocnicza metoda budująca inspekcję testową
    // =======================================================

    private Inspection buildInspection(String title, ScopeType scopeType, UUID scopeId) {
        Inspection i = new Inspection();
        i.setId(UUID.randomUUID());
        i.setTitle(title);
        i.setDescription("Opis przeglądu");
        i.setScheduledAt(LocalDateTime.of(2026, 6, 15, 10, 0));
        i.setScopeType(scopeType);
        i.setScopeId(scopeId);
        i.setCreatedBy(zarzadca);
        i.setCreatedAt(LocalDateTime.now());
        return i;
    }

    // =======================================================
    // Tworzenie przeglądu
    // =======================================================

    @Nested
    @DisplayName("create — tworzenie przeglądu")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zapisuje przegląd i zwraca DTO")
        void shouldCreateInspectionAndReturnDto() {
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd kominiarski",
                            "Opis",
                            LocalDateTime.of(2026, 7, 1, 9, 0),
                            ScopeType.BUDYNEK,
                            buildingId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(buildingRepository.existsById(buildingId)).thenReturn(true);
            when(inspectionRepository.save(any(Inspection.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            InspectionResponse result = inspectionService.create(request, ZARZADCA_EMAIL);

            assertThat(result.getTitle()).isEqualTo("Przegląd kominiarski");
            assertThat(result.getScopeType()).isEqualTo(ScopeType.BUDYNEK);
            assertThat(result.getScopeId()).isEqualTo(buildingId);
            assertThat(result.getCreatedByName()).isEqualTo("Adam Zarządca");
        }

        @Test
        @DisplayName("Zapisuje encję z wszystkimi polami z żądania")
        void shouldPersistAllFieldsFromRequest() {
            LocalDateTime scheduledAt = LocalDateTime.of(2026, 8, 1, 14, 0);
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd elektryczny",
                            "Szczegółowy opis",
                            scheduledAt,
                            ScopeType.KLATKA,
                            staircaseId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(staircaseRepository.existsById(staircaseId)).thenReturn(true);
            when(inspectionRepository.save(any(Inspection.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            inspectionService.create(request, ZARZADCA_EMAIL);

            ArgumentCaptor<Inspection> captor = ArgumentCaptor.forClass(Inspection.class);
            verify(inspectionRepository).save(captor.capture());
            Inspection saved = captor.getValue();

            assertThat(saved.getTitle()).isEqualTo("Przegląd elektryczny");
            assertThat(saved.getDescription()).isEqualTo("Szczegółowy opis");
            assertThat(saved.getScheduledAt()).isEqualTo(scheduledAt);
            assertThat(saved.getScopeType()).isEqualTo(ScopeType.KLATKA);
            assertThat(saved.getScopeId()).isEqualTo(staircaseId);
            assertThat(saved.getCreatedBy()).isEqualTo(zarzadca);
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowWhenUserNotFound() {
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd",
                            null,
                            LocalDateTime.now().plusDays(10),
                            ScopeType.BUDYNEK,
                            buildingId);

            when(userRepository.findByEmail("nieznany@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inspectionService.create(request, "nieznany@blokur.pl"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Użytkownik nie istnieje");

            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący budynek — rzuca NotFoundException")
        void shouldThrowWhenBuildingNotFound() {
            UUID unknownBuildingId = UUID.randomUUID();
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd",
                            null,
                            LocalDateTime.now().plusDays(10),
                            ScopeType.BUDYNEK,
                            unknownBuildingId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(buildingRepository.existsById(unknownBuildingId)).thenReturn(false);

            assertThatThrownBy(() -> inspectionService.create(request, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Budynek o ID");

            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — rzuca NotFoundException")
        void shouldThrowWhenPropertyNotFound() {
            UUID unknownPropertyId = UUID.randomUUID();
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd",
                            null,
                            LocalDateTime.now().plusDays(10),
                            ScopeType.NIERUCHOMOSC,
                            unknownPropertyId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(propertyRepository.existsById(unknownPropertyId)).thenReturn(false);

            assertThatThrownBy(() -> inspectionService.create(request, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Nieruchomość o ID");

            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejąca klatka — rzuca NotFoundException")
        void shouldThrowWhenStaircaseNotFound() {
            UUID unknownStaircaseId = UUID.randomUUID();
            InspectionRequest request =
                    new InspectionRequest(
                            "Przegląd",
                            null,
                            LocalDateTime.now().plusDays(10),
                            ScopeType.KLATKA,
                            unknownStaircaseId);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(staircaseRepository.existsById(unknownStaircaseId)).thenReturn(false);

            assertThatThrownBy(() -> inspectionService.create(request, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Klatka schodowa o ID");

            verify(inspectionRepository, never()).save(any());
        }
    }

    // =======================================================
    // Pobieranie przeglądów
    // =======================================================

    @Nested
    @DisplayName("getAll — pobieranie listy przeglądów")
    class GetAllTests {

        @Test
        @DisplayName("Zarządca — zwraca wszystkie przeglądy")
        void shouldReturnAllInspectionsForZarzadca() {
            List<Inspection> all =
                    List.of(
                            buildInspection("Przegląd gazowy", ScopeType.BUDYNEK, buildingId),
                            buildInspection(
                                    "Przegląd kominiarki", ScopeType.NIERUCHOMOSC, propertyId));

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(inspectionRepository.findAllByOrderByScheduledAtAsc()).thenReturn(all);

            List<InspectionResponse> result = inspectionService.getAll(ZARZADCA_EMAIL);

            assertThat(result).hasSize(2);
            verify(inspectionRepository).findAllByOrderByScheduledAtAsc();
            verify(inspectionRepository, never()).findForUser(any(), any(), any());
        }

        @Test
        @DisplayName("Mieszkaniec z lokalem — filtruje przeglądy przez zasięg hierarchii")
        void shouldFilterInspectionsByUserHierarchy() {
            List<Inspection> filtered = List.of(sampleInspection);

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(inspectionRepository.findForUser(staircaseId, buildingId, propertyId))
                    .thenReturn(filtered);

            List<InspectionResponse> result = inspectionService.getAll(MIESZKANIEC_EMAIL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Przegląd gazowy");
            verify(inspectionRepository).findForUser(staircaseId, buildingId, propertyId);
        }

        @Test
        @DisplayName("Mieszkaniec bez lokalu — zwraca pustą listę")
        void shouldReturnEmptyListWhenUserHasNoApartment() {
            mieszkaniec.setUserApartments(new ArrayList<>());

            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));

            List<InspectionResponse> result = inspectionService.getAll(MIESZKANIEC_EMAIL);

            assertThat(result).isEmpty();
            verify(inspectionRepository, never()).findForUser(any(), any(), any());
            verify(inspectionRepository, never()).findAllByOrderByScheduledAtAsc();
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — zwraca pustą listę")
        void shouldReturnEmptyListWhenUserNotFound() {
            when(userRepository.findByEmail("nieznany@blokur.pl")).thenReturn(Optional.empty());

            List<InspectionResponse> result = inspectionService.getAll("nieznany@blokur.pl");

            assertThat(result).isEmpty();
            verify(inspectionRepository, never()).findAllByOrderByScheduledAtAsc();
            verify(inspectionRepository, never()).findForUser(any(), any(), any());
        }
    }

    // =======================================================
    // Aktualizacja przeglądu
    // =======================================================

    @Nested
    @DisplayName("update — aktualizacja przeglądu")
    class UpdateTests {

        @Test
        @DisplayName("Poprawne dane — aktualizuje pola i zwraca DTO")
        void shouldUpdateInspectionAndReturnDto() {
            InspectionRequest request =
                    new InspectionRequest(
                            "Nowy tytuł",
                            "Nowy opis",
                            LocalDateTime.of(2026, 9, 1, 11, 0),
                            ScopeType.NIERUCHOMOSC,
                            propertyId);

            when(inspectionRepository.findById(inspectionId))
                    .thenReturn(Optional.of(sampleInspection));
            when(propertyRepository.existsById(propertyId)).thenReturn(true);
            when(inspectionRepository.save(any(Inspection.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            InspectionResponse result = inspectionService.update(inspectionId, request);

            assertThat(result.getTitle()).isEqualTo("Nowy tytuł");
            assertThat(result.getScopeType()).isEqualTo(ScopeType.NIERUCHOMOSC);
            assertThat(result.getScopeId()).isEqualTo(propertyId);
        }

        @Test
        @DisplayName("Nieistniejący przegląd — rzuca NotFoundException")
        void shouldThrowWhenInspectionNotFound() {
            InspectionRequest request =
                    new InspectionRequest(
                            "Tytuł",
                            null,
                            LocalDateTime.now().plusDays(5),
                            ScopeType.BUDYNEK,
                            buildingId);

            when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inspectionService.update(inspectionId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Przegląd o ID");

            verify(inspectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący zasięg przy aktualizacji — rzuca NotFoundException")
        void shouldThrowWhenScopeNotFoundOnUpdate() {
            UUID unknownId = UUID.randomUUID();
            InspectionRequest request =
                    new InspectionRequest(
                            "Tytuł",
                            null,
                            LocalDateTime.now().plusDays(5),
                            ScopeType.BUDYNEK,
                            unknownId);

            when(inspectionRepository.findById(inspectionId))
                    .thenReturn(Optional.of(sampleInspection));
            when(buildingRepository.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> inspectionService.update(inspectionId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Budynek o ID");

            verify(inspectionRepository, never()).save(any());
        }
    }

    // =======================================================
    // Usuwanie przeglądu
    // =======================================================

    @Nested
    @DisplayName("delete — usuwanie przeglądu")
    class DeleteTests {

        @Test
        @DisplayName("Istniejący przegląd — usuwa encję")
        void shouldDeleteExistingInspection() {
            when(inspectionRepository.existsById(inspectionId)).thenReturn(true);

            inspectionService.delete(inspectionId);

            verify(inspectionRepository).deleteById(inspectionId);
        }

        @Test
        @DisplayName("Nieistniejący przegląd — rzuca NotFoundException bez wywołania delete")
        void shouldThrowWhenInspectionNotFound() {
            when(inspectionRepository.existsById(inspectionId)).thenReturn(false);

            assertThatThrownBy(() -> inspectionService.delete(inspectionId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Przegląd o ID");

            verify(inspectionRepository, never()).deleteById(any());
        }
    }

    // =======================================================
    // Mapowanie na DTO
    // =======================================================

    @Nested
    @DisplayName("Mapowanie encji na DTO")
    class MappingTests {

        @Test
        @DisplayName("Wszystkie pola są poprawnie mapowane na InspectionResponse")
        void shouldMapAllFieldsToDto() {
            LocalDateTime scheduled = LocalDateTime.of(2026, 6, 15, 10, 0);
            Inspection inspection =
                    buildInspection("Przegląd gazowy", ScopeType.BUDYNEK, buildingId);
            inspection.setScheduledAt(scheduled);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(inspectionRepository.findAllByOrderByScheduledAtAsc())
                    .thenReturn(List.of(inspection));

            List<InspectionResponse> result = inspectionService.getAll(ZARZADCA_EMAIL);

            InspectionResponse dto = result.get(0);
            assertThat(dto.getTitle()).isEqualTo("Przegląd gazowy");
            assertThat(dto.getScopeType()).isEqualTo(ScopeType.BUDYNEK);
            assertThat(dto.getScopeId()).isEqualTo(buildingId);
            assertThat(dto.getScheduledAt()).isEqualTo(scheduled);
            assertThat(dto.getCreatedByName()).isEqualTo("Adam Zarządca");
        }

        @Test
        @DisplayName("Przegląd bez twórcy — createdByName jest pustym Stringiem")
        void shouldReturnEmptyCreatedByNameWhenCreatorIsNull() {
            sampleInspection.setCreatedBy(null);

            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(inspectionRepository.findAllByOrderByScheduledAtAsc())
                    .thenReturn(List.of(sampleInspection));

            List<InspectionResponse> result = inspectionService.getAll(ZARZADCA_EMAIL);

            assertThat(result.get(0).getCreatedByName()).isEmpty();
        }
    }
}

package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import pl.edu.ur.blokur.dto.ApartmentRequest;
import pl.edu.ur.blokur.dto.ApartmentResponse;
import pl.edu.ur.blokur.dto.BuildingRequest;
import pl.edu.ur.blokur.dto.BuildingResponse;
import pl.edu.ur.blokur.dto.BuildingTreeDto;
import pl.edu.ur.blokur.dto.StaircaseRequest;
import pl.edu.ur.blokur.dto.StaircaseResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Property;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.PropertyRepository;
import pl.edu.ur.blokur.repository.StaircaseRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildingService")
class BuildingServiceTest {

    @Mock private BuildingRepository buildingRepository;
    @Mock private StaircaseRepository staircaseRepository;
    @Mock private ApartmentRepository apartmentRepository;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private BuildingService buildingService;

    private UUID buildingId;
    private UUID staircaseId;
    private UUID apartmentId;
    private UUID propertyId;

    private Building building;
    private Staircase staircase;
    private Apartment apartment;
    private Property property;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        staircaseId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        property = new Property();
        property.setId(propertyId);
        property.setName("Nieruchomość testowa");

        building = new Building();
        building.setId(buildingId);
        building.setEstateName("Osiedle Testowe");
        building.setName("Budynek A");
        building.setAddress("ul. Testowa 1");
        building.setLatitude(new BigDecimal("50.123456"));
        building.setLongitude(new BigDecimal("22.123456"));
        building.setStaircases(new ArrayList<>());

        staircase = new Staircase();
        staircase.setId(staircaseId);
        staircase.setLabel("A");
        staircase.setBuilding(building);
        staircase.setApartments(new ArrayList<>());

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("101");
        apartment.setFloor(1);
        apartment.setAreaM2(new BigDecimal("45.50"));
        apartment.setOwnershipType("WLASNOSCIOWY");
        apartment.setStaircase(staircase);
    }

    // =========================================================
    // getBuildingTree
    // =========================================================

    @Nested
    @DisplayName("getBuildingTree — drzewo hierarchii")
    class GetBuildingTree {

        @Test
        @DisplayName("Zwraca listę budynków z klatkami i lokalami")
        void shouldReturnMappedTree() {
            staircase.getApartments().add(apartment);
            building.getStaircases().add(staircase);

            when(buildingRepository.findAll()).thenReturn(List.of(building));

            List<BuildingTreeDto> result = buildingService.getBuildingTree();

            assertThat(result).hasSize(1);
            BuildingTreeDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(buildingId);
            assertThat(dto.getName()).isEqualTo("Budynek A");
            assertThat(dto.getStaircases()).hasSize(1);

            BuildingTreeDto.StaircaseDto stDto = dto.getStaircases().get(0);
            assertThat(stDto.getLabel()).isEqualTo("A");
            assertThat(stDto.getApartments()).hasSize(1);

            BuildingTreeDto.ApartmentDto aptDto = stDto.getApartments().get(0);
            assertThat(aptDto.getNumber()).isEqualTo("101");
            assertThat(aptDto.getFloor()).isEqualTo(1);
            assertThat(aptDto.getAreaM2()).isEqualByComparingTo("45.50");
            assertThat(aptDto.getOwnershipType()).isEqualTo("WLASNOSCIOWY");
        }

        @Test
        @DisplayName("Pusta baza — zwraca pustą listę")
        void shouldReturnEmptyListWhenNoBuildings() {
            when(buildingRepository.findAll()).thenReturn(List.of());

            List<BuildingTreeDto> result = buildingService.getBuildingTree();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================
    // createBuilding
    // =========================================================

    @Nested
    @DisplayName("createBuilding — tworzenie budynku")
    class CreateBuilding {

        @Test
        @DisplayName("Poprawne dane bez propertyId — tworzy budynek")
        void shouldCreateBuildingWithoutProperty() {
            BuildingRequest request = new BuildingRequest();
            request.setEstateName("Osiedle Testowe");
            request.setName("Budynek A");
            request.setAddress("ul. Testowa 1");
            request.setLatitude(new BigDecimal("50.123456"));
            request.setLongitude(new BigDecimal("22.123456"));

            when(buildingRepository.save(any(Building.class))).thenReturn(building);

            BuildingResponse response = buildingService.createBuilding(request);

            assertThat(response.getId()).isEqualTo(buildingId);
            assertThat(response.getName()).isEqualTo("Budynek A");
            assertThat(response.getPropertyId()).isNull();
            verify(buildingRepository).save(any(Building.class));
        }

        @Test
        @DisplayName("Podano propertyId — przypisuje nieruchomość")
        void shouldCreateBuildingWithProperty() {
            BuildingRequest request = new BuildingRequest();
            request.setEstateName("Osiedle Testowe");
            request.setName("Budynek A");
            request.setAddress("ul. Testowa 1");
            request.setPropertyId(propertyId);

            building.setProperty(property);
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(buildingRepository.save(any(Building.class))).thenReturn(building);

            BuildingResponse response = buildingService.createBuilding(request);

            assertThat(response.getPropertyId()).isEqualTo(propertyId);
        }

        @Test
        @DisplayName("Nieistniejące propertyId — rzuca NotFoundException")
        void shouldThrowWhenPropertyNotFound() {
            BuildingRequest request = new BuildingRequest();
            request.setEstateName("X");
            request.setName("X");
            request.setAddress("X");
            request.setPropertyId(propertyId);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> buildingService.createBuilding(request))
                    .isInstanceOf(NotFoundException.class);
            verify(buildingRepository, never()).save(any());
        }
    }

    // =========================================================
    // updateBuilding
    // =========================================================

    @Nested
    @DisplayName("updateBuilding — edycja budynku")
    class UpdateBuilding {

        @Test
        @DisplayName("Poprawne dane — aktualizuje budynek")
        void shouldUpdateBuilding() {
            BuildingRequest request = new BuildingRequest();
            request.setEstateName("Nowe Osiedle");
            request.setName("Budynek B");
            request.setAddress("ul. Nowa 2");

            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(buildingRepository.save(any(Building.class))).thenReturn(building);

            BuildingResponse response = buildingService.updateBuilding(buildingId, request);

            ArgumentCaptor<Building> captor = ArgumentCaptor.forClass(Building.class);
            verify(buildingRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Budynek B");
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Nieistniejący budynek — rzuca NotFoundException")
        void shouldThrowWhenBuildingNotFound() {
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () -> buildingService.updateBuilding(buildingId, new BuildingRequest()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // deleteBuilding
    // =========================================================

    @Nested
    @DisplayName("deleteBuilding — usunięcie budynku")
    class DeleteBuilding {

        @Test
        @DisplayName("Budynek bez lokali — usuwa")
        void shouldDeleteBuildingWithNoApartments() {
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

            buildingService.deleteBuilding(buildingId);

            verify(buildingRepository).delete(building);
        }

        @Test
        @DisplayName("Budynek z lokalami — rzuca BusinessValidationException")
        void shouldThrowWhenBuildingHasApartments() {
            staircase.getApartments().add(apartment);
            building.getStaircases().add(staircase);
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

            assertThatThrownBy(() -> buildingService.deleteBuilding(buildingId))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("lokale");
            verify(buildingRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Nieistniejący budynek — rzuca NotFoundException")
        void shouldThrowWhenBuildingNotFound() {
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> buildingService.deleteBuilding(buildingId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // createStaircase
    // =========================================================

    @Nested
    @DisplayName("createStaircase — tworzenie klatki schodowej")
    class CreateStaircase {

        @Test
        @DisplayName("Poprawne dane — tworzy klatkę w budynku")
        void shouldCreateStaircase() {
            StaircaseRequest request = new StaircaseRequest();
            request.setLabel("B");

            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(staircaseRepository.save(any(Staircase.class))).thenReturn(staircase);

            StaircaseResponse response = buildingService.createStaircase(buildingId, request);

            assertThat(response.getBuildingId()).isEqualTo(buildingId);
            verify(staircaseRepository).save(any(Staircase.class));
        }

        @Test
        @DisplayName("Nieistniejący budynek — rzuca NotFoundException")
        void shouldThrowWhenBuildingNotFound() {
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    buildingService.createStaircase(
                                            buildingId, new StaircaseRequest()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // updateStaircase
    // =========================================================

    @Nested
    @DisplayName("updateStaircase — edycja klatki schodowej")
    class UpdateStaircase {

        @Test
        @DisplayName("Poprawne dane — aktualizuje etykietę klatki")
        void shouldUpdateStaircase() {
            StaircaseRequest request = new StaircaseRequest();
            request.setLabel("C");

            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(staircaseRepository.save(any(Staircase.class))).thenReturn(staircase);

            StaircaseResponse response =
                    buildingService.updateStaircase(buildingId, staircaseId, request);

            assertThat(response.getLabel()).isEqualTo("C");
        }

        @Test
        @DisplayName("Klatka należy do innego budynku — rzuca NotFoundException")
        void shouldThrowWhenStaircaseBelongsToDifferentBuilding() {
            Building otherBuilding = new Building();
            otherBuilding.setId(UUID.randomUUID());
            staircase.setBuilding(otherBuilding);

            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));

            assertThatThrownBy(
                            () ->
                                    buildingService.updateStaircase(
                                            buildingId, staircaseId, new StaircaseRequest()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // deleteStaircase
    // =========================================================

    @Nested
    @DisplayName("deleteStaircase — usunięcie klatki schodowej")
    class DeleteStaircase {

        @Test
        @DisplayName("Klatka bez lokali — usuwa")
        void shouldDeleteStaircaseWithNoApartments() {
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));

            buildingService.deleteStaircase(buildingId, staircaseId);

            verify(staircaseRepository).delete(staircase);
        }

        @Test
        @DisplayName("Klatka z lokalami — rzuca BusinessValidationException")
        void shouldThrowWhenStaircaseHasApartments() {
            staircase.getApartments().add(apartment);
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));

            assertThatThrownBy(() -> buildingService.deleteStaircase(buildingId, staircaseId))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("lokale");
            verify(staircaseRepository, never()).delete(any());
        }
    }

    // =========================================================
    // createApartment
    // =========================================================

    @Nested
    @DisplayName("createApartment — tworzenie lokalu")
    class CreateApartment {

        @Test
        @DisplayName("Poprawne dane — tworzy lokal z wszystkimi polami")
        void shouldCreateApartmentWithAllFields() {
            ApartmentRequest request = new ApartmentRequest();
            request.setNumber("101");
            request.setFloor(1);
            request.setAreaM2(new BigDecimal("45.50"));
            request.setOwnershipType("WLASNOSCIOWY");

            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.save(any(Apartment.class))).thenReturn(apartment);

            ApartmentResponse response = buildingService.createApartment(staircaseId, request);

            assertThat(response.getNumber()).isEqualTo("101");
            assertThat(response.getFloor()).isEqualTo(1);
            assertThat(response.getAreaM2()).isEqualByComparingTo("45.50");
            assertThat(response.getOwnershipType()).isEqualTo("WLASNOSCIOWY");
            assertThat(response.getStaircaseId()).isEqualTo(staircaseId);
            verify(apartmentRepository).save(any(Apartment.class));
        }

        @Test
        @DisplayName("Tylko numer lokalu — tworzy lokal z polami opcjonalnymi null")
        void shouldCreateApartmentWithMinimalData() {
            ApartmentRequest request = new ApartmentRequest();
            request.setNumber("102");

            Apartment minimalApartment = new Apartment();
            minimalApartment.setId(UUID.randomUUID());
            minimalApartment.setNumber("102");
            minimalApartment.setStaircase(staircase);

            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.save(any(Apartment.class))).thenReturn(minimalApartment);

            ApartmentResponse response = buildingService.createApartment(staircaseId, request);

            assertThat(response.getNumber()).isEqualTo("102");
            assertThat(response.getFloor()).isNull();
            assertThat(response.getAreaM2()).isNull();
            assertThat(response.getOwnershipType()).isNull();
        }

        @Test
        @DisplayName("Nieistniejąca klatka — rzuca NotFoundException")
        void shouldThrowWhenStaircaseNotFound() {
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    buildingService.createApartment(
                                            staircaseId, new ApartmentRequest()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // updateApartment
    // =========================================================

    @Nested
    @DisplayName("updateApartment — edycja lokalu")
    class UpdateApartment {

        @Test
        @DisplayName("Poprawne dane — aktualizuje lokal")
        void shouldUpdateApartment() {
            ApartmentRequest request = new ApartmentRequest();
            request.setNumber("202");
            request.setFloor(2);
            request.setAreaM2(new BigDecimal("60.00"));
            request.setOwnershipType("NAJEM");

            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(apartmentRepository.save(any(Apartment.class))).thenReturn(apartment);

            ApartmentResponse response =
                    buildingService.updateApartment(staircaseId, apartmentId, request);

            ArgumentCaptor<Apartment> captor = ArgumentCaptor.forClass(Apartment.class);
            verify(apartmentRepository).save(captor.capture());
            assertThat(captor.getValue().getNumber()).isEqualTo("202");
            assertThat(captor.getValue().getOwnershipType()).isEqualTo("NAJEM");
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Lokal należy do innej klatki — rzuca NotFoundException")
        void shouldThrowWhenApartmentBelongsToDifferentStaircase() {
            Staircase otherStaircase = new Staircase();
            otherStaircase.setId(UUID.randomUUID());
            apartment.setStaircase(otherStaircase);

            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

            assertThatThrownBy(
                            () ->
                                    buildingService.updateApartment(
                                            staircaseId, apartmentId, new ApartmentRequest()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowWhenApartmentNotFound() {
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    buildingService.updateApartment(
                                            staircaseId, apartmentId, new ApartmentRequest()))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // deleteApartment
    // =========================================================

    @Nested
    @DisplayName("deleteApartment — usunięcie lokalu")
    class DeleteApartment {

        @Test
        @DisplayName("Poprawne dane — usuwa lokal")
        void shouldDeleteApartment() {
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

            buildingService.deleteApartment(staircaseId, apartmentId);

            verify(apartmentRepository).delete(apartment);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowWhenApartmentNotFound() {
            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> buildingService.deleteApartment(staircaseId, apartmentId))
                    .isInstanceOf(NotFoundException.class);
            verify(apartmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Lokal należy do innej klatki — rzuca NotFoundException")
        void shouldThrowWhenApartmentBelongsToDifferentStaircase() {
            Staircase otherStaircase = new Staircase();
            otherStaircase.setId(UUID.randomUUID());
            apartment.setStaircase(otherStaircase);

            when(staircaseRepository.findById(staircaseId)).thenReturn(Optional.of(staircase));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

            assertThatThrownBy(() -> buildingService.deleteApartment(staircaseId, apartmentId))
                    .isInstanceOf(NotFoundException.class);
            verify(apartmentRepository, never()).delete(any());
        }
    }
}

package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.PropertyRepository;
import pl.edu.ur.blokur.repository.StaircaseRepository;

/** Serwis realizujący logikę biznesową dla obiektów struktury budynków. */
@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final StaircaseRepository staircaseRepository;
    private final PropertyRepository propertyRepository;

    public BuildingService(
            BuildingRepository buildingRepository,
            StaircaseRepository staircaseRepository,
            PropertyRepository propertyRepository) {
        this.buildingRepository = buildingRepository;
        this.staircaseRepository = staircaseRepository;
        this.propertyRepository = propertyRepository;
    }

    /**
     * Zwraca pełne drzewo hierarchii budynków.
     *
     * @return lista przetransformowanych budynków
     */
    @Transactional(readOnly = true)
    public List<BuildingTreeDto> getBuildingTree() {
        List<Building> buildings = buildingRepository.findAll();
        return buildings.stream().map(this::mapToBuildingTreeDto).collect(Collectors.toList());
    }

    /**
     * Tworzy nowy budynek. Jeśli podano propertyId, przypisuje budynek do nieruchomości.
     *
     * @param request dane nowego budynku
     * @return utworzony budynek
     */
    @Transactional
    public BuildingResponse createBuilding(BuildingRequest request) {
        Building building = new Building();
        applyBuildingRequest(building, request);
        buildingRepository.save(building);
        return mapToBuildingResponse(building);
    }

    /**
     * Aktualizuje dane istniejącego budynku.
     *
     * @param id identyfikator budynku
     * @param request nowe dane budynku
     * @return zaktualizowany budynek
     */
    @Transactional
    public BuildingResponse updateBuilding(UUID id, BuildingRequest request) {
        Building building = findBuildingOrThrow(id);
        applyBuildingRequest(building, request);
        buildingRepository.save(building);
        return mapToBuildingResponse(building);
    }

    /**
     * Usuwa budynek. Operacja dozwolona tylko gdy budynek nie ma żadnych lokali mieszkalnych.
     *
     * @param id identyfikator budynku
     * @throws BusinessValidationException gdy budynek ma powiązane lokale
     */
    @Transactional
    public void deleteBuilding(UUID id) {
        Building building = findBuildingOrThrow(id);
        boolean hasApartments =
                building.getStaircases().stream()
                        .anyMatch(s -> s.getApartments() != null && !s.getApartments().isEmpty());
        if (hasApartments) {
            throw new BusinessValidationException(
                    "Nie można usunąć budynku, który posiada lokale mieszkalne");
        }
        buildingRepository.delete(building);
    }

    /**
     * Tworzy nową klatkę schodową w budynku.
     *
     * @param buildingId identyfikator budynku
     * @param request dane nowej klatki
     * @return utworzona klatka schodowa
     */
    @Transactional
    public StaircaseResponse createStaircase(UUID buildingId, StaircaseRequest request) {
        Building building = findBuildingOrThrow(buildingId);
        Staircase staircase = new Staircase();
        staircase.setLabel(request.getLabel());
        staircase.setBuilding(building);
        staircaseRepository.save(staircase);
        return mapToStaircaseResponse(staircase);
    }

    /**
     * Aktualizuje dane klatki schodowej.
     *
     * @param buildingId identyfikator budynku (weryfikacja przynależności)
     * @param staircaseId identyfikator klatki
     * @param request nowe dane klatki
     * @return zaktualizowana klatka schodowa
     */
    @Transactional
    public StaircaseResponse updateStaircase(
            UUID buildingId, UUID staircaseId, StaircaseRequest request) {
        Staircase staircase = findStaircaseInBuilding(buildingId, staircaseId);
        staircase.setLabel(request.getLabel());
        staircaseRepository.save(staircase);
        return mapToStaircaseResponse(staircase);
    }

    /**
     * Usuwa klatkę schodową. Operacja dozwolona tylko gdy klatka nie ma żadnych lokali.
     *
     * @param buildingId identyfikator budynku (weryfikacja przynależności)
     * @param staircaseId identyfikator klatki
     * @throws BusinessValidationException gdy klatka ma powiązane lokale
     */
    @Transactional
    public void deleteStaircase(UUID buildingId, UUID staircaseId) {
        Staircase staircase = findStaircaseInBuilding(buildingId, staircaseId);
        if (staircase.getApartments() != null && !staircase.getApartments().isEmpty()) {
            throw new BusinessValidationException(
                    "Nie można usunąć klatki schodowej, która posiada lokale mieszkalne");
        }
        staircaseRepository.delete(staircase);
    }

    private void applyBuildingRequest(Building building, BuildingRequest request) {
        building.setEstateName(request.getEstateName());
        building.setName(request.getName());
        building.setAddress(request.getAddress());
        building.setLatitude(request.getLatitude());
        building.setLongitude(request.getLongitude());
        if (request.getPropertyId() != null) {
            Property property =
                    propertyRepository
                            .findById(request.getPropertyId())
                            .orElseThrow(
                                    () ->
                                            new NotFoundException(
                                                    "Nieruchomość o id "
                                                            + request.getPropertyId()
                                                            + " nie istnieje"));
            building.setProperty(property);
        } else {
            building.setProperty(null);
        }
    }

    private Building findBuildingOrThrow(UUID id) {
        return buildingRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Budynek o id " + id + " nie istnieje"));
    }

    private Staircase findStaircaseInBuilding(UUID buildingId, UUID staircaseId) {
        findBuildingOrThrow(buildingId);
        Staircase staircase =
                staircaseRepository
                        .findById(staircaseId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Klatka o id " + staircaseId + " nie istnieje"));
        if (!staircase.getBuilding().getId().equals(buildingId)) {
            throw new NotFoundException(
                    "Klatka o id " + staircaseId + " nie należy do budynku " + buildingId);
        }
        return staircase;
    }

    private BuildingResponse mapToBuildingResponse(Building building) {
        BuildingResponse response = new BuildingResponse();
        response.setId(building.getId());
        response.setEstateName(building.getEstateName());
        response.setName(building.getName());
        response.setAddress(building.getAddress());
        response.setLatitude(building.getLatitude());
        response.setLongitude(building.getLongitude());
        if (building.getProperty() != null) {
            response.setPropertyId(building.getProperty().getId());
        }
        return response;
    }

    private StaircaseResponse mapToStaircaseResponse(Staircase staircase) {
        StaircaseResponse response = new StaircaseResponse();
        response.setId(staircase.getId());
        response.setLabel(staircase.getLabel());
        response.setBuildingId(staircase.getBuilding().getId());
        return response;
    }

    private BuildingTreeDto mapToBuildingTreeDto(Building building) {
        BuildingTreeDto dto = new BuildingTreeDto();
        dto.setId(building.getId());
        dto.setEstateName(building.getEstateName());
        dto.setName(building.getName());
        dto.setAddress(building.getAddress());
        dto.setLatitude(building.getLatitude());
        dto.setLongitude(building.getLongitude());

        if (building.getStaircases() != null) {
            List<BuildingTreeDto.StaircaseDto> staircases =
                    building.getStaircases().stream()
                            .map(this::mapToStaircaseDto)
                            .collect(Collectors.toList());
            dto.setStaircases(staircases);
        }

        return dto;
    }

    private BuildingTreeDto.StaircaseDto mapToStaircaseDto(Staircase staircase) {
        BuildingTreeDto.StaircaseDto dto = new BuildingTreeDto.StaircaseDto();
        dto.setId(staircase.getId());
        dto.setLabel(staircase.getLabel());

        if (staircase.getApartments() != null) {
            List<BuildingTreeDto.ApartmentDto> apartments =
                    staircase.getApartments().stream()
                            .map(this::mapToApartmentDto)
                            .collect(Collectors.toList());
            dto.setApartments(apartments);
        }

        return dto;
    }

    private BuildingTreeDto.ApartmentDto mapToApartmentDto(Apartment apartment) {
        BuildingTreeDto.ApartmentDto dto = new BuildingTreeDto.ApartmentDto();
        dto.setId(apartment.getId());
        dto.setNumber(apartment.getNumber());
        dto.setFloor(apartment.getFloor());
        dto.setAreaM2(apartment.getAreaM2());
        dto.setOwnershipType(apartment.getOwnershipType());
        dto.setCurrentBalance(apartment.getCurrentBalance());
        return dto;
    }
}

package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.BuildingTreeDto;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.repository.BuildingRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis realizujący logikę biznesową dla obiektów struktury budynków.
 */
@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    /**
     * Zwraca pełne drzewo hierarchii budynków.
     * @return lista przetransformowanych budynków
     */
    @Transactional(readOnly = true)
    public List<BuildingTreeDto> getBuildingTree() {
        List<Building> buildings = buildingRepository.findAll();
        return buildings.stream()
            .map(this::mapToBuildingTreeDto)
            .collect(Collectors.toList());
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
            List<BuildingTreeDto.StaircaseDto> staircases = building.getStaircases().stream()
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
            List<BuildingTreeDto.ApartmentDto> apartments = staircase.getApartments().stream()
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
        dto.setCurrentBalance(apartment.getCurrentBalance());
        return dto;
    }
}

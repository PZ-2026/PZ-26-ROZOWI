package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
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
 * Serwis dostarczający logikę biznesową dla modułu przeglądów technicznych. Obsługuje tworzenie,
 * pobieranie, aktualizację i usuwanie przeglądów z uwzględnieniem zasięgu użytkownika.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final StaircaseRepository staircaseRepository;

    /**
     * Tworzy instancję serwisu z wymaganymi repozytoriami.
     *
     * @param inspectionRepository repozytorium przeglądów
     * @param userRepository repozytorium użytkowników
     * @param propertyRepository repozytorium nieruchomości
     * @param buildingRepository repozytorium budynków
     * @param staircaseRepository repozytorium klatek schodowych
     */
    public InspectionService(
            InspectionRepository inspectionRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            BuildingRepository buildingRepository,
            StaircaseRepository staircaseRepository) {
        this.inspectionRepository = inspectionRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.staircaseRepository = staircaseRepository;
    }

    /**
     * Tworzy nowy przegląd techniczny. Waliduje istnienie encji wskazanej przez zasięg.
     *
     * @param request dane nowego przeglądu
     * @param username email zalogowanego zarządcy
     * @return DTO utworzonego przeglądu
     * @throws NotFoundException gdy użytkownik lub encja zasięgu nie istnieje
     */
    public InspectionResponse create(InspectionRequest request, String username) {
        User creator =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        validateScopeExists(request.getScopeType(), request.getScopeId());

        Inspection inspection = new Inspection();
        inspection.setTitle(request.getTitle());
        inspection.setDescription(request.getDescription());
        inspection.setScheduledAt(request.getScheduledAt());
        inspection.setScopeType(request.getScopeType());
        inspection.setScopeId(request.getScopeId());
        inspection.setCreatedBy(creator);

        return mapToResponse(inspectionRepository.save(inspection));
    }

    /**
     * Zwraca listę przeglądów filtrowaną według zasięgu zalogowanego użytkownika. Zarządca (rola
     * ZARZADCA) widzi wszystkie przeglądy. Pozostałe role widzą przeglądy pasujące do ich
     * hierarchii: klatka → budynek → nieruchomość.
     *
     * @param username email zalogowanego użytkownika
     * @return lista przeglądów w formie DTO
     */
    public List<InspectionResponse> getAll(String username) {
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            return List.of();
        }

        if ("ZARZADCA".equals(user.getRole())) {
            return inspectionRepository.findAllByOrderByScheduledAtAsc().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        UUID staircaseId = null;
        UUID buildingId = null;
        UUID propertyId = null;

        if (!user.getUserApartments().isEmpty()) {
            UserApartment ua = user.getUserApartments().get(0);
            Apartment apt = ua.getApartment();
            if (apt != null) {
                Staircase sc = apt.getStaircase();
                if (sc != null) {
                    staircaseId = sc.getId();
                    Building b = sc.getBuilding();
                    if (b != null) {
                        buildingId = b.getId();
                        Property p = b.getProperty();
                        if (p != null) {
                            propertyId = p.getId();
                        }
                    }
                }
            }
        }

        if (staircaseId == null && buildingId == null && propertyId == null) {
            return List.of();
        }

        return inspectionRepository.findForUser(staircaseId, buildingId, propertyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Aktualizuje istniejący przegląd techniczny. Waliduje istnienie encji wskazanej przez zasięg.
     *
     * @param id identyfikator przeglądu do aktualizacji
     * @param request nowe dane przeglądu
     * @return DTO zaktualizowanego przeglądu
     * @throws NotFoundException gdy przegląd lub encja zasięgu nie istnieje
     */
    public InspectionResponse update(UUID id, InspectionRequest request) {
        Inspection inspection =
                inspectionRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Przegląd o ID " + id + " nie istnieje"));

        validateScopeExists(request.getScopeType(), request.getScopeId());

        inspection.setTitle(request.getTitle());
        inspection.setDescription(request.getDescription());
        inspection.setScheduledAt(request.getScheduledAt());
        inspection.setScopeType(request.getScopeType());
        inspection.setScopeId(request.getScopeId());

        return mapToResponse(inspectionRepository.save(inspection));
    }

    /**
     * Usuwa przegląd techniczny na podstawie identyfikatora.
     *
     * @param id identyfikator przeglądu do usunięcia
     * @throws NotFoundException gdy przegląd nie istnieje
     */
    public void delete(UUID id) {
        if (!inspectionRepository.existsById(id)) {
            throw new NotFoundException("Przegląd o ID " + id + " nie istnieje");
        }
        inspectionRepository.deleteById(id);
    }

    /**
     * Sprawdza, czy encja wskazana przez typ i identyfikator zasięgu istnieje w bazie danych.
     *
     * @param scopeType typ zasięgu przeglądu
     * @param scopeId UUID encji zasięgu
     * @throws NotFoundException gdy encja o podanym ID nie istnieje
     */
    private void validateScopeExists(ScopeType scopeType, UUID scopeId) {
        switch (scopeType) {
            case NIERUCHOMOSC -> {
                if (!propertyRepository.existsById(scopeId)) {
                    throw new NotFoundException("Nieruchomość o ID " + scopeId + " nie istnieje");
                }
            }
            case BUDYNEK -> {
                if (!buildingRepository.existsById(scopeId)) {
                    throw new NotFoundException("Budynek o ID " + scopeId + " nie istnieje");
                }
            }
            case KLATKA -> {
                if (!staircaseRepository.existsById(scopeId)) {
                    throw new NotFoundException(
                            "Klatka schodowa o ID " + scopeId + " nie istnieje");
                }
            }
        }
    }

    /**
     * Mapuje encję przeglądu na obiekt DTO.
     *
     * @param i encja przeglądu do zmapowania
     * @return obiekt DTO gotowy do serializacji
     */
    private InspectionResponse mapToResponse(Inspection i) {
        String createdByName = "";
        if (i.getCreatedBy() != null) {
            createdByName = i.getCreatedBy().getFirstName() + " " + i.getCreatedBy().getLastName();
        }
        return new InspectionResponse(
                i.getId(),
                i.getTitle(),
                i.getDescription(),
                i.getScheduledAt(),
                i.getScopeType(),
                i.getScopeId(),
                createdByName,
                i.getCreatedAt());
    }
}

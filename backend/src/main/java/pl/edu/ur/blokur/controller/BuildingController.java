package pl.edu.ur.blokur.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.BuildingTreeDto;
import pl.edu.ur.blokur.service.BuildingService;

import java.util.List;

/**
 * Kontroler zarządzający API dla budynków i hierarchii osiedla.
 */
@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    /**
     * Endpoint zwracający zagnieżdżoną strukturę drzewa: Budynek -> Klatki -> Lokale.
     * Wykorzystywane np. w panelu SPA Zarządcy.
     *
     * @return hierarchiczne dane budynków
     */
    @GetMapping("/tree")
    public ResponseEntity<List<BuildingTreeDto>> getBuildingTree() {
        List<BuildingTreeDto> tree = buildingService.getBuildingTree();
        return ResponseEntity.ok(tree);
    }
}

package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/** DTO reprezentujące drzewo struktury budynku: budynek → klatki schodowe → lokale. */
@Data
public class BuildingTreeDto {

    private UUID id;
    private String estateName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<StaircaseDto> staircases;

    /** DTO reprezentujące klatkę schodową w drzewie budynku. */
    @Data
    public static class StaircaseDto {
        private UUID id;
        private String label;
        private List<ApartmentDto> apartments;
    }

    /** DTO reprezentujące lokal w drzewie budynku. */
    @Data
    public static class ApartmentDto {
        private UUID id;
        private String number;
        private Integer floor;
        private BigDecimal areaM2;
        private String ownershipType;
        private BigDecimal currentBalance;
    }
}

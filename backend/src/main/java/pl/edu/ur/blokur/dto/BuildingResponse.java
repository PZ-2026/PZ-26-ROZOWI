package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi budynku zwracanymi przez API po operacjach tworzenia i edycji. */
@Data
public class BuildingResponse {

    private UUID id;
    private UUID propertyId;
    private String estateName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

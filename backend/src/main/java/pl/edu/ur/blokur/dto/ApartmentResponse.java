package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/** DTO z danymi lokalu zwracanymi przez API po operacjach tworzenia i edycji. */
@Data
public class ApartmentResponse {

    private UUID id;
    private UUID staircaseId;
    private String number;
    private Integer floor;
    private BigDecimal areaM2;
    private String ownershipType;
    private BigDecimal currentBalance;
}

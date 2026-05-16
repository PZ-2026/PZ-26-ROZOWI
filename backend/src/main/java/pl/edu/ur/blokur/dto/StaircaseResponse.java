package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO z danymi klatki schodowej zwracanymi przez API po operacjach tworzenia i edycji. */
@Data
public class StaircaseResponse {

    private UUID id;
    private UUID buildingId;
    private String label;
}

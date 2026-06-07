package pl.edu.ur.blokur.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import pl.edu.ur.blokur.models.MediumType;

/** DTO z danymi licznika zwracanymi przez API. */
@Data
public class MeterResponse {

    private UUID id;
    private UUID apartmentId;
    private String serialNumber;
    private MediumType mediumType;
    private LocalDate installationDate;
    private boolean active;

    /**
     * Tworzy odpowiedź z danymi licznika.
     *
     * @param id identyfikator licznika
     * @param apartmentId identyfikator lokalu, do którego należy licznik
     * @param serialNumber numer seryjny licznika
     * @param mediumType typ medium
     * @param installationDate data montażu
     * @param active czy licznik jest aktywny
     */
    public MeterResponse(
            UUID id,
            UUID apartmentId,
            String serialNumber,
            MediumType mediumType,
            LocalDate installationDate,
            boolean active) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.serialNumber = serialNumber;
        this.mediumType = mediumType;
        this.installationDate = installationDate;
        this.active = active;
    }
}

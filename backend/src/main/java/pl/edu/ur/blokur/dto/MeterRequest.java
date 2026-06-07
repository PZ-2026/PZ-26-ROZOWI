package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import pl.edu.ur.blokur.models.MediumType;

/** DTO z danymi nowego licznika przesyłanymi przez klienta. */
@Data
public class MeterRequest {

    @NotBlank(message = "Numer seryjny licznika nie może być pusty")
    @Size(max = 100, message = "Numer seryjny nie może przekraczać 100 znaków")
    private String serialNumber;

    @NotNull(message = "Typ medium jest wymagany")
    private MediumType mediumType;

    @NotNull(message = "Data montażu jest wymagana")
    private LocalDate installationDate;

    /**
     * Tworzy żądanie rejestracji nowego licznika.
     *
     * @param serialNumber numer seryjny licznika
     * @param mediumType typ medium (np. WODA, GAZ, ENERGIA)
     * @param installationDate data montażu licznika
     */
    public MeterRequest(String serialNumber, MediumType mediumType, LocalDate installationDate) {
        this.serialNumber = serialNumber;
        this.mediumType = mediumType;
        this.installationDate = installationDate;
    }
}

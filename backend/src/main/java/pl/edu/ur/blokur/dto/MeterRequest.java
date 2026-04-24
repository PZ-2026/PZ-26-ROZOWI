package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import pl.edu.ur.blokur.models.MediumType;

/** DTO z danymi nowego licznika przesyłanymi przez klienta. */
public class MeterRequest {

    @NotBlank(message = "Numer seryjny licznika nie może być pusty")
    @Size(max = 100, message = "Numer seryjny nie może przekraczać 100 znaków")
    private String serialNumber;

    @NotNull(message = "Typ medium jest wymagany")
    private MediumType mediumType;

    @NotNull(message = "Data montażu jest wymagana")
    private LocalDate installationDate;

    public MeterRequest() {}

    public MeterRequest(String serialNumber, MediumType mediumType, LocalDate installationDate) {
        this.serialNumber = serialNumber;
        this.mediumType = mediumType;
        this.installationDate = installationDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public MediumType getMediumType() {
        return mediumType;
    }

    public void setMediumType(MediumType mediumType) {
        this.mediumType = mediumType;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }
}

package pl.edu.ur.blokur.dto;

import java.time.LocalDate;
import java.util.UUID;
import pl.edu.ur.blokur.models.MediumType;

/** DTO z danymi licznika zwracanymi przez API. */
public class MeterResponse {

    private UUID id;
    private UUID apartmentId;
    private String serialNumber;
    private MediumType mediumType;
    private LocalDate installationDate;
    private boolean active;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(UUID apartmentId) {
        this.apartmentId = apartmentId;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

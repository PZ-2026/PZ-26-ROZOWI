package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Obiekt transferu danych (DTO) reprezentujący drzewo budynku.
 */
public class BuildingTreeDto {

    private UUID id;
    private String estateName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<StaircaseDto> staircases;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEstateName() {
        return estateName;
    }

    public void setEstateName(String estateName) {
        this.estateName = estateName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public List<StaircaseDto> getStaircases() {
        return staircases;
    }

    public void setStaircases(List<StaircaseDto> staircases) {
        this.staircases = staircases;
    }

    public static class StaircaseDto {
        private UUID id;
        private String label;
        private List<ApartmentDto> apartments;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<ApartmentDto> getApartments() {
            return apartments;
        }

        public void setApartments(List<ApartmentDto> apartments) {
            this.apartments = apartments;
        }
    }

    public static class ApartmentDto {
        private UUID id;
        private String number;
        private BigDecimal currentBalance;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        public void setCurrentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
        }
    }
}

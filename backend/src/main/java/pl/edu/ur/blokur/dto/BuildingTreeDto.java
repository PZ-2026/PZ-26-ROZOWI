package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** DTO reprezentujące drzewo struktury budynku: budynek → klatki schodowe → lokale. */
public class BuildingTreeDto {

    private UUID id;
    private String estateName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<StaircaseDto> staircases;

    /**
     * Zwraca unikalny identyfikator budynku.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator budynku.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca nazwę osiedla/wspólnoty.
     *
     * @return nazwa osiedla
     */
    public String getEstateName() {
        return estateName;
    }

    /**
     * Ustawia nazwę osiedla/wspólnoty.
     *
     * @param estateName nazwa osiedla
     */
    public void setEstateName(String estateName) {
        this.estateName = estateName;
    }

    /**
     * Zwraca nazwę budynku.
     *
     * @return nazwa budynku
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę budynku.
     *
     * @param name nazwa budynku
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Zwraca adres budynku.
     *
     * @return adres budynku
     */
    public String getAddress() {
        return address;
    }

    /**
     * Ustawia adres budynku.
     *
     * @param address adres budynku
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Zwraca szerokość geograficzną lokalizacji budynku.
     *
     * @return szerokość geograficzna
     */
    public BigDecimal getLatitude() {
        return latitude;
    }

    /**
     * Ustawia szerokość geograficzną lokalizacji budynku.
     *
     * @param latitude szerokość geograficzna
     */
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    /**
     * Zwraca długość geograficzną lokalizacji budynku.
     *
     * @return długość geograficzna
     */
    public BigDecimal getLongitude() {
        return longitude;
    }

    /**
     * Ustawia długość geograficzną lokalizacji budynku.
     *
     * @param longitude długość geograficzna
     */
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    /**
     * Zwraca listę klatek schodowych w budynku.
     *
     * @return lista klatek schodowych
     */
    public List<StaircaseDto> getStaircases() {
        return staircases;
    }

    /**
     * Ustawia listę klatek schodowych w budynku.
     *
     * @param staircases lista klatek schodowych
     */
    public void setStaircases(List<StaircaseDto> staircases) {
        this.staircases = staircases;
    }

    /** DTO reprezentujące klatkę schodową w drzewie budynku. */
    public static class StaircaseDto {
        private UUID id;
        private String label;
        private List<ApartmentDto> apartments;

        /**
         * Zwraca unikalny identyfikator klatki schodowej.
         *
         * @return identyfikator UUID
         */
        public UUID getId() {
            return id;
        }

        /**
         * Ustawia unikalny identyfikator klatki schodowej.
         *
         * @param id identyfikator UUID
         */
        public void setId(UUID id) {
            this.id = id;
        }

        /**
         * Zwraca etykietę klatki schodowej.
         *
         * @return etykieta klatki (np. "A", "B")
         */
        public String getLabel() {
            return label;
        }

        /**
         * Ustawia etykietę klatki schodowej.
         *
         * @param label etykieta klatki
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * Zwraca listę lokali w klatce schodowej.
         *
         * @return lista lokali
         */
        public List<ApartmentDto> getApartments() {
            return apartments;
        }

        /**
         * Ustawia listę lokali w klatce schodowej.
         *
         * @param apartments lista lokali
         */
        public void setApartments(List<ApartmentDto> apartments) {
            this.apartments = apartments;
        }
    }

    /** DTO reprezentujące lokal w drzewie budynku. */
    public static class ApartmentDto {
        private UUID id;
        private String number;
        private Integer floor;
        private BigDecimal areaM2;
        private String ownershipType;
        private BigDecimal currentBalance;

        /**
         * Zwraca unikalny identyfikator lokalu.
         *
         * @return identyfikator UUID
         */
        public UUID getId() {
            return id;
        }

        /**
         * Ustawia unikalny identyfikator lokalu.
         *
         * @param id identyfikator UUID
         */
        public void setId(UUID id) {
            this.id = id;
        }

        /**
         * Zwraca numer lokalu.
         *
         * @return numer lokalu
         */
        public String getNumber() {
            return number;
        }

        /**
         * Ustawia numer lokalu.
         *
         * @param number numer lokalu
         */
        public void setNumber(String number) {
            this.number = number;
        }

        /**
         * Zwraca piętro lokalu.
         *
         * @return numer piętra lub {@code null}
         */
        public Integer getFloor() {
            return floor;
        }

        /**
         * Ustawia piętro lokalu.
         *
         * @param floor numer piętra
         */
        public void setFloor(Integer floor) {
            this.floor = floor;
        }

        /**
         * Zwraca powierzchnię lokalu w m².
         *
         * @return powierzchnia lub {@code null}
         */
        public BigDecimal getAreaM2() {
            return areaM2;
        }

        /**
         * Ustawia powierzchnię lokalu w m².
         *
         * @param areaM2 powierzchnia w m²
         */
        public void setAreaM2(BigDecimal areaM2) {
            this.areaM2 = areaM2;
        }

        /**
         * Zwraca typ własności lokalu.
         *
         * @return WLASNOSCIOWY, NAJEM lub {@code null}
         */
        public String getOwnershipType() {
            return ownershipType;
        }

        /**
         * Ustawia typ własności lokalu.
         *
         * @param ownershipType WLASNOSCIOWY lub NAJEM
         */
        public void setOwnershipType(String ownershipType) {
            this.ownershipType = ownershipType;
        }

        /**
         * Zwraca aktualne saldo rozliczeniowe lokalu.
         *
         * @return saldo w PLN
         */
        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        /**
         * Ustawia aktualne saldo rozliczeniowe lokalu.
         *
         * @param currentBalance saldo w PLN
         */
        public void setCurrentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
        }
    }
}

package pl.edu.ur.blokur.dto;

import java.util.UUID;

/** DTO z danymi klatki schodowej zwracanymi przez API po operacjach tworzenia i edycji. */
public class StaircaseResponse {

    private UUID id;
    private UUID buildingId;
    private String label;

    /** Konstruktor bezargumentowy wymagany przez serializację JSON. */
    public StaircaseResponse() {}

    /**
     * Zwraca identyfikator klatki schodowej.
     *
     * @return UUID klatki
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator klatki schodowej.
     *
     * @param id UUID klatki
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca identyfikator budynku, do którego należy klatka.
     *
     * @return UUID budynku
     */
    public UUID getBuildingId() {
        return buildingId;
    }

    /**
     * Ustawia identyfikator budynku, do którego należy klatka.
     *
     * @param buildingId UUID budynku
     */
    public void setBuildingId(UUID buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * Zwraca etykietę klatki schodowej.
     *
     * @return etykieta klatki
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
}

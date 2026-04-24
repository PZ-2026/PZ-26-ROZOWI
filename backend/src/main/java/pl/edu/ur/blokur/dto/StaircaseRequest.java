package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO z danymi klatki schodowej przesyłanymi przez klienta (tworzenie i aktualizacja). */
public class StaircaseRequest {

    @NotBlank(message = "Etykieta klatki nie może być pusta")
    @Size(max = 50, message = "Etykieta klatki nie może przekraczać 50 znaków")
    private String label;

    /** Konstruktor bezargumentowy wymagany przez deserializację JSON. */
    public StaircaseRequest() {}

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
}

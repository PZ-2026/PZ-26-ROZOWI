package pl.edu.ur.blokur.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;

/**
 * Encja reprezentująca konkretną opcję wyboru (np. "Za", "Przeciw")
 * w ramach danej uchwały.
 */
@Entity
@Table(name = "resolution_options", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id", "resolution_id"})
})
public class ResolutionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolution_id", nullable = false)
    private Resolution resolution;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    /**
     * Zwraca identyfikator opcji.
     *
     * @return identyfikator UUID opcji
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator opcji.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca uchwałę, z którą powiązana jest ta opcja.
     *
     * @return instancja Resolution
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Ustawia powiązaną uchwałę dla opcji głosowania.
     *
     * @param resolution Uchwała do powiązania
     */
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * Zwraca tekst/nazwę opcji dostepnej w ankiecie.
     *
     * @return String jako nazwa dająca wyraz i wybór.
     */
    public String getOptionText() {
        return optionText;
    }

    /**
     * Ustawia tekst określający opcję, w którą celuje ten rekord bazy danych.
     *
     * @param optionText String z tekskem wyboru, np "za", "wstrzymał się"
     */
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}

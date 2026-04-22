package pl.edu.ur.blokur.models;

<<<<<<< Updated upstream
import jakarta.persistence.CascadeType;
=======
>>>>>>> Stashed changes
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
<<<<<<< Updated upstream
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encja reprezentująca kategorię zgłoszenia (np. awaria, usterka, inne).
 */
=======
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

>>>>>>> Stashed changes
@Entity
@Table(name = "ticket_categories")
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

<<<<<<< Updated upstream
    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    /**
     * Zwraca identyfikator kategorii.
     *
     * @return identyfikator UUID
     */
=======
    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive = true;

>>>>>>> Stashed changes
    public UUID getId() {
        return id;
    }

<<<<<<< Updated upstream
    /**
     * Ustawia identyfikator kategorii.
     *
     * @param id identyfikator UUID
     */
=======
>>>>>>> Stashed changes
    public void setId(UUID id) {
        this.id = id;
    }

<<<<<<< Updated upstream
    /**
     * Zwraca nazwę kategorii zgłoszenia.
     *
     * @return nazwa kategorii
     */
=======
>>>>>>> Stashed changes
    public String getName() {
        return name;
    }

<<<<<<< Updated upstream
    /**
     * Ustawia nazwę kategorii zgłoszenia.
     *
     * @param name nazwa kategorii
     */
=======
>>>>>>> Stashed changes
    public void setName(String name) {
        this.name = name;
    }

<<<<<<< Updated upstream
    /**
     * Zwraca listę zgłoszeń przypisanych do tej kategorii.
     *
     * @return lista zgłoszeń
     */
    public List<Ticket> getTickets() {
        return tickets;
    }

    /**
     * Ustawia listę zgłoszeń przypisanych do tej kategorii.
     *
     * @param tickets lista zgłoszeń
     */
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
=======
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
>>>>>>> Stashed changes
    }
}

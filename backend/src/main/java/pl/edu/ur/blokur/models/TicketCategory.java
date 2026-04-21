package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encja reprezentująca kategorię zgłoszenia (np. awaria, usterka, inne).
 */
@Entity
@Table(name = "ticket_categories")
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    /**
     * Zwraca identyfikator kategorii.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia identyfikator kategorii.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca nazwę kategorii zgłoszenia.
     *
     * @return nazwa kategorii
     */
    public String getName() {
        return name;
    }

    /**
     * Ustawia nazwę kategorii zgłoszenia.
     *
     * @param name nazwa kategorii
     */
    public void setName(String name) {
        this.name = name;
    }

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
    }
}

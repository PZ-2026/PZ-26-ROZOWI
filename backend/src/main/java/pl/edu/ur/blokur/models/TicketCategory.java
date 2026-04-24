package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca kategorię zgłoszenia (np. awaria, usterka, inne). */
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

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    /**
     * Zwraca unikalny identyfikator kategorii.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator kategorii.
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
     * Informuje, czy kategoria jest aktywna (widoczna dla użytkowników).
     *
     * @return {@code true} jeśli kategoria jest aktywna
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Ustawia flagę aktywności kategorii (soft delete gdy {@code false}).
     *
     * @param active {@code false} aby ukryć kategorię przed użytkownikami
     */
    public void setActive(boolean active) {
        isActive = active;
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

package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca powiązanie użytkownika z lokalem mieszkalnym. */
@Entity
@Table(name = "user_apartments")
public class UserApartment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    /**
     * Zwraca unikalny identyfikator powiązania.
     *
     * @return identyfikator UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Ustawia unikalny identyfikator powiązania.
     *
     * @param id identyfikator UUID
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Zwraca użytkownika powiązanego z lokalem.
     *
     * @return encja użytkownika
     */
    public User getUser() {
        return user;
    }

    /**
     * Ustawia użytkownika powiązanego z lokalem.
     *
     * @param user encja użytkownika
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Zwraca lokal powiązany z użytkownikiem.
     *
     * @return encja lokalu
     */
    public Apartment getApartment() {
        return apartment;
    }

    /**
     * Ustawia lokal powiązany z użytkownikiem.
     *
     * @param apartment encja lokalu
     */
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
}

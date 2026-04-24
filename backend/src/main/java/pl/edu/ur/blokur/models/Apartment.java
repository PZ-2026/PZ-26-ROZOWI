package pl.edu.ur.blokur.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca lokal mieszkalny lub użytkowy w budynku. */
@Entity
@Table(name = "apartments")
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "number", nullable = false, length = 50)
    private String number;

    @ColumnDefault("0.00")
    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staircase_id", nullable = false)
    private Staircase staircase;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserApartment> userApartments = new ArrayList<>();

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

    /**
     * Zwraca klatkę schodową, w której znajduje się lokal.
     *
     * @return encja klatki schodowej
     */
    public Staircase getStaircase() {
        return staircase;
    }

    /**
     * Ustawia klatkę schodową, w której znajduje się lokal.
     *
     * @param staircase encja klatki schodowej
     */
    public void setStaircase(Staircase staircase) {
        this.staircase = staircase;
    }

    /**
     * Zwraca listę przypisań użytkowników do tego lokalu.
     *
     * @return lista powiązań UserApartment
     */
    public List<UserApartment> getUserApartments() {
        return userApartments;
    }

    /**
     * Ustawia listę przypisań użytkowników do tego lokalu.
     *
     * @param userApartments lista powiązań UserApartment
     */
    public void setUserApartments(List<UserApartment> userApartments) {
        this.userApartments = userApartments;
    }

    /**
     * Aktualizuje saldo lokalu poprzez dodanie podanej kwoty. Dodatnia kwota zwiększa saldo
     * (wpłata), ujemna zmniejsza (naliczenie).
     *
     * @param amount kwota do dodania do salda
     */
    public void updateBalance(BigDecimal amount) {
        if (amount != null) {
            if (this.currentBalance == null) {
                this.currentBalance = BigDecimal.ZERO;
            }
            this.currentBalance = this.currentBalance.add(amount);
        }
    }
}

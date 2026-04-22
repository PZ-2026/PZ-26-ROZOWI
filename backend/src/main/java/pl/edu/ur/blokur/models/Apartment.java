package pl.edu.ur.blokur.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Encja reprezentująca lokal mieszkalny lub użytkowy w budynku.
 */
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

    public Staircase getStaircase() {
        return staircase;
    }

    public void setStaircase(Staircase staircase) {
        this.staircase = staircase;
    }

    public List<UserApartment> getUserApartments() {
        return userApartments;
    }

    public void setUserApartments(List<UserApartment> userApartments) {
        this.userApartments = userApartments;
    }

    /**
     * Aktualizuje saldo lokalu poprzez dodanie podanej kwoty.
     * Dodatnia kwota zwiększa saldo (wpłata), ujemna zmniejsza (naliczenie).
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

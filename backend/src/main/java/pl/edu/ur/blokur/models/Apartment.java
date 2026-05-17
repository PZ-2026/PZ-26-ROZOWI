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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/** Encja reprezentująca lokal mieszkalny lub użytkowy w budynku. */
@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "area_m2", precision = 6, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "ownership_type", length = 20)
    private String ownershipType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staircase_id", nullable = false)
    private Staircase staircase;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserApartment> userApartments = new ArrayList<>();

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

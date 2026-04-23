package pl.edu.ur.blokur.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe dla encji {@link Apartment}.
 * Weryfikują logikę aktualizacji salda lokalu.
 */
@DisplayName("Apartment — encja lokalu mieszkalnego")
class ApartmentTest {

    @Test
    @DisplayName("updateBalance — dodawanie kwoty dodatniej zwiększa saldo")
    void shouldIncreaseBalanceWhenAddingPositiveAmount() {
        Apartment apartment = new Apartment();
        apartment.setCurrentBalance(new BigDecimal("1000.00"));

        apartment.updateBalance(new BigDecimal("500.00"));

        assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("updateBalance — dodawanie kwoty ujemnej zmniejsza saldo")
    void shouldDecreaseBalanceWhenAddingNegativeAmount() {
        Apartment apartment = new Apartment();
        apartment.setCurrentBalance(new BigDecimal("1000.00"));

        apartment.updateBalance(new BigDecimal("-300.00"));

        assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("updateBalance — inicjalizacja salda gdy jest null")
    void shouldInitializeBalanceWhenCurrentIsNull() {
        Apartment apartment = new Apartment();
        apartment.setCurrentBalance(null);

        apartment.updateBalance(new BigDecimal("200.00"));

        assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("200.00");
    }
}

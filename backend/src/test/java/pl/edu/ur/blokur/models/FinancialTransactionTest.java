package pl.edu.ur.blokur.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testy jednostkowe encji {@link FinancialTransaction}. Weryfikują poprawność getterów, setterów
 * oraz domyślny stan obiektu.
 */
@DisplayName("FinancialTransaction — encja transakcji finansowej")
class FinancialTransactionTest {

    private FinancialTransaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new FinancialTransaction();
    }

    @Test
    @DisplayName("Nowy obiekt — wszystkie pola mają wartość null")
    void shouldHaveNullFieldsWhenCreated() {
        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getApartment()).isNull();
        assertThat(transaction.getType()).isNull();
        assertThat(transaction.getAmount()).isNull();
        assertThat(transaction.getDescription()).isNull();
        assertThat(transaction.getTransactionDate()).isNull();
        assertThat(transaction.getRecordedBy()).isNull();
    }

    @Test
    @DisplayName("Setter i getter id — poprawne przypisanie UUID")
    void shouldSetAndGetId() {
        UUID id = UUID.randomUUID();
        transaction.setId(id);
        assertThat(transaction.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Setter i getter apartment — poprawne powiązanie z lokalem")
    void shouldSetAndGetApartment() {
        Apartment apartment = new Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setNumber("5");

        transaction.setApartment(apartment);

        assertThat(transaction.getApartment()).isEqualTo(apartment);
        assertThat(transaction.getApartment().getNumber()).isEqualTo("5");
    }

    @Test
    @DisplayName("Setter i getter type — poprawne przypisanie typu")
    void shouldSetAndGetType() {
        transaction.setType("WPLATA");
        assertThat(transaction.getType()).isEqualTo("WPLATA");
    }

    @Test
    @DisplayName("Setter i getter amount — poprawne przypisanie kwoty")
    void shouldSetAndGetAmount() {
        BigDecimal amount = new BigDecimal("1250.50");
        transaction.setAmount(amount);
        assertThat(transaction.getAmount()).isEqualByComparingTo("1250.50");
    }

    @Test
    @DisplayName("Setter i getter description — poprawne przypisanie opisu")
    void shouldSetAndGetDescription() {
        transaction.setDescription("Wpłata czynszu za marzec");
        assertThat(transaction.getDescription()).isEqualTo("Wpłata czynszu za marzec");
    }

    @Test
    @DisplayName("Setter i getter transactionDate — poprawne przypisanie daty")
    void shouldSetAndGetTransactionDate() {
        LocalDate date = LocalDate.of(2026, 4, 1);
        transaction.setTransactionDate(date);
        assertThat(transaction.getTransactionDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("Setter i getter recordedBy — poprawne powiązanie z użytkownikiem")
    void shouldSetAndGetRecordedBy() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("zarzadca@blokur.pl");

        transaction.setRecordedBy(user);

        assertThat(transaction.getRecordedBy()).isEqualTo(user);
        assertThat(transaction.getRecordedBy().getEmail()).isEqualTo("zarzadca@blokur.pl");
    }

    @Test
    @DisplayName("Kwota ujemna — setAmount akceptuje wartości ujemne")
    void shouldAcceptNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-300.00");
        transaction.setAmount(negativeAmount);
        assertThat(transaction.getAmount()).isEqualByComparingTo("-300.00");
    }
}

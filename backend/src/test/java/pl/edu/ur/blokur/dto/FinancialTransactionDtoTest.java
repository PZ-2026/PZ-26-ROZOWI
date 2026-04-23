package pl.edu.ur.blokur.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy jednostkowe klas DTO modułu transakcji finansowych.
 * Weryfikują konstruktory, gettery, settery oraz domyślny stan obiektów.
 */
@DisplayName("DTO transakcji finansowych")
class FinancialTransactionDtoTest {

    // =======================================================
    // FinancialTransactionRequest
    // =======================================================

    @Nested
    @DisplayName("FinancialTransactionRequest")
    class RequestTests {

        @Test
        @DisplayName("Konstruktor bezargumentowy — pola mają wartość null")
        void shouldCreateEmptyRequest() {
            FinancialTransactionRequest request = new FinancialTransactionRequest();

            assertThat(request.getType()).isNull();
            assertThat(request.getAmount()).isNull();
            assertThat(request.getDescription()).isNull();
            assertThat(request.getTransactionDate()).isNull();
        }

        @Test
        @DisplayName("Konstruktor z argumentami — poprawne przypisanie pól")
        void shouldCreateRequestWithAllFields() {
            FinancialTransactionRequest request = new FinancialTransactionRequest(
                "WPLATA",
                new BigDecimal("500.00"),
                "Wpłata czynszu",
                LocalDate.of(2026, 4, 15)
            );

            assertThat(request.getType()).isEqualTo("WPLATA");
            assertThat(request.getAmount()).isEqualByComparingTo("500.00");
            assertThat(request.getDescription()).isEqualTo("Wpłata czynszu");
            assertThat(request.getTransactionDate())
                .isEqualTo(LocalDate.of(2026, 4, 15));
        }

        @Test
        @DisplayName("Settery — nadpisują wartości ustawione konstruktorem")
        void shouldOverrideFieldsWithSetters() {
            FinancialTransactionRequest request = new FinancialTransactionRequest(
                "WPLATA",
                new BigDecimal("500.00"),
                "Stary opis",
                LocalDate.of(2026, 1, 1)
            );

            request.setType("NALICZENIE");
            request.setAmount(new BigDecimal("-300.00"));
            request.setDescription("Nowy opis");
            request.setTransactionDate(LocalDate.of(2026, 5, 1));

            assertThat(request.getType()).isEqualTo("NALICZENIE");
            assertThat(request.getAmount()).isEqualByComparingTo("-300.00");
            assertThat(request.getDescription()).isEqualTo("Nowy opis");
            assertThat(request.getTransactionDate())
                .isEqualTo(LocalDate.of(2026, 5, 1));
        }
    }

    // =======================================================
    // FinancialTransactionResponse
    // =======================================================

    @Nested
    @DisplayName("FinancialTransactionResponse")
    class ResponseTests {

        @Test
        @DisplayName("Konstruktor — poprawne przypisanie wszystkich pól")
        void shouldCreateResponseWithAllFields() {
            java.util.UUID id = java.util.UUID.randomUUID();
            java.util.UUID apartmentId = java.util.UUID.randomUUID();

            FinancialTransactionResponse response = new FinancialTransactionResponse(
                id, apartmentId, "WPLATA",
                new BigDecimal("500.00"),
                "Wpłata czynszu",
                LocalDate.of(2026, 4, 15),
                "zarzadca@blokur.pl"
            );

            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getApartmentId()).isEqualTo(apartmentId);
            assertThat(response.getType()).isEqualTo("WPLATA");
            assertThat(response.getAmount()).isEqualByComparingTo("500.00");
            assertThat(response.getDescription()).isEqualTo("Wpłata czynszu");
            assertThat(response.getTransactionDate())
                .isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(response.getRecordedByEmail())
                .isEqualTo("zarzadca@blokur.pl");
        }

        @Test
        @DisplayName("Settery — nadpisują wartości ustawione konstruktorem")
        void shouldOverrideFieldsWithSetters() {
            java.util.UUID id = java.util.UUID.randomUUID();
            java.util.UUID newId = java.util.UUID.randomUUID();

            FinancialTransactionResponse response = new FinancialTransactionResponse(
                id, java.util.UUID.randomUUID(), "WPLATA",
                new BigDecimal("500.00"),
                "Opis",
                LocalDate.of(2026, 4, 15),
                "old@blokur.pl"
            );

            response.setId(newId);
            response.setType("NALICZENIE");
            response.setRecordedByEmail("new@blokur.pl");

            assertThat(response.getId()).isEqualTo(newId);
            assertThat(response.getType()).isEqualTo("NALICZENIE");
            assertThat(response.getRecordedByEmail()).isEqualTo("new@blokur.pl");
        }
    }

    // =======================================================
    // ApartmentTransactionsResponse
    // =======================================================

    @Nested
    @DisplayName("ApartmentTransactionsResponse")
    class ApartmentResponseTests {

        @Test
        @DisplayName("Konstruktor — poprawne przypisanie salda i listy transakcji")
        void shouldCreateWithBalanceAndTransactions() {
            FinancialTransactionResponse tx = new FinancialTransactionResponse(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                "WPLATA",
                new BigDecimal("500.00"),
                "Wpłata",
                LocalDate.of(2026, 4, 15),
                "zarzadca@blokur.pl"
            );

            ApartmentTransactionsResponse response =
                new ApartmentTransactionsResponse(
                    new BigDecimal("700.00"),
                    java.util.List.of(tx)
                );

            assertThat(response.getCurrentBalance())
                .isEqualByComparingTo("700.00");
            assertThat(response.getTransactions()).hasSize(1);
            assertThat(response.getTransactions().get(0).getType())
                .isEqualTo("WPLATA");
        }

        @Test
        @DisplayName("Pusta lista transakcji — saldo bez transakcji")
        void shouldCreateWithEmptyTransactionList() {
            ApartmentTransactionsResponse response =
                new ApartmentTransactionsResponse(
                    new BigDecimal("0.00"),
                    java.util.List.of()
                );

            assertThat(response.getCurrentBalance())
                .isEqualByComparingTo("0.00");
            assertThat(response.getTransactions()).isEmpty();
        }

        @Test
        @DisplayName("Settery — nadpisują wartości")
        void shouldOverrideFieldsWithSetters() {
            ApartmentTransactionsResponse response =
                new ApartmentTransactionsResponse(
                    new BigDecimal("100.00"),
                    java.util.List.of()
                );

            response.setCurrentBalance(new BigDecimal("999.99"));
            response.setTransactions(null);

            assertThat(response.getCurrentBalance())
                .isEqualByComparingTo("999.99");
            assertThat(response.getTransactions()).isNull();
        }
    }
}

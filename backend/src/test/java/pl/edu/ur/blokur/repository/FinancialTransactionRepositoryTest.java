package pl.edu.ur.blokur.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.FinancialTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy integracyjne warstwy repozytorium dla {@link FinancialTransactionRepository}.
 * Weryfikują poprawność zapytań SQL, mapowania JPA oraz sortowania wyników.
 * Wykorzystują bazę H2 w profilu 'test'.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FinancialTransactionRepository — testy bazy danych")
class FinancialTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Test
    @DisplayName("findByApartmentIdOrderByTransactionDateDesc — zwraca transakcje posortowane malejąco po dacie")
    void shouldFindTransactionsByApartmentIdOrderedByDateDesc() {
        // Given
        Apartment app1 = createApartment("1A");
        Apartment app2 = createApartment("2B");

        createTransaction(app1, "WPLATA", "100.00", LocalDate.of(2026, 4, 1));
        createTransaction(app1, "NALICZENIE", "200.00", LocalDate.of(2026, 4, 10));
        createTransaction(app1, "WPLATA", "50.00", LocalDate.of(2026, 4, 5));
        
        createTransaction(app2, "WPLATA", "500.00", LocalDate.of(2026, 4, 15));

        // When
        List<FinancialTransaction> results = 
            transactionRepository.findByApartmentIdOrderByTransactionDateDesc(app1.getId());

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getTransactionDate()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(results.get(1).getTransactionDate()).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(results.get(2).getTransactionDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        
        for (FinancialTransaction t : results) {
            assertThat(t.getApartment().getId()).isEqualTo(app1.getId());
        }
    }

    @Test
    @DisplayName("findByApartmentId — zwraca pustą listę gdy brak transakcji")
    void shouldReturnEmptyListWhenNoTransactions() {
        UUID randomId = UUID.randomUUID();
        
        List<FinancialTransaction> results = 
            transactionRepository.findByApartmentIdOrderByTransactionDateDesc(randomId);
            
        assertThat(results).isEmpty();
    }

    private Apartment createApartment(String number) {
        Apartment apartment = new Apartment();
        apartment.setNumber(number);
        apartment.setCurrentBalance(BigDecimal.ZERO);
        return entityManager.persist(apartment);
    }

    private void createTransaction(Apartment apartment, String type, String amount, LocalDate date) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setApartment(apartment);
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setDescription("Test transaction");
        transaction.setTransactionDate(date);
        entityManager.persist(transaction);
    }
}

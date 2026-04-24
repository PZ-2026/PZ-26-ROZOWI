package pl.edu.ur.blokur.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.FinancialTransaction;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;

/**
 * Testy integracyjne warstwy repozytorium dla {@link FinancialTransactionRepository}. Weryfikują
 * poprawność zapytań SQL, mapowania JPA oraz sortowania wyników. Wykorzystują bazę H2 w profilu
 * 'test'.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("FinancialTransactionRepository — testy bazy danych")
class FinancialTransactionRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private FinancialTransactionRepository transactionRepository;

    private Staircase staircase;
    private User recorder;

    @BeforeEach
    void setUp() {
        Building building = new Building();
        building.setEstateName("Osiedle Testowe");
        building.setName("Budynek A");
        building.setAddress("ul. Testowa 1");
        entityManager.persist(building);

        staircase = new Staircase();
        staircase.setLabel("I");
        staircase.setBuilding(building);
        entityManager.persist(staircase);

        recorder = new User();
        recorder.setEmail("zarzadca@test.pl");
        recorder.setPasswordHash("hash");
        recorder.setFirstName("Jan");
        recorder.setLastName("Kowalski");
        recorder.setRole("ZARZADCA");
        entityManager.persist(recorder);
    }

    @Test
    @DisplayName(
            "findByApartmentIdOrderByTransactionDateDesc — zwraca transakcje posortowane malejąco"
                    + " po dacie")
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
        apartment.setStaircase(staircase);
        return entityManager.persist(apartment);
    }

    private void createTransaction(
            Apartment apartment, String type, String amount, LocalDate date) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setApartment(apartment);
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setDescription("Test transaction");
        transaction.setTransactionDate(date);
        transaction.setRecordedBy(recorder);
        entityManager.persist(transaction);
    }
}

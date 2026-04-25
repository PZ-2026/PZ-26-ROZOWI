package pl.edu.ur.blokur.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.FinancialTransaction;

/**
 * Repozytorium JPA dla encji {@link FinancialTransaction}. Udostępnia metody dostępu do historii
 * transakcji finansowych lokalu.
 */
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    /**
     * Pobiera listę transakcji finansowych dla wskazanego lokalu, posortowaną malejąco po dacie
     * transakcji.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista transakcji posortowana od najnowszej
     */
    List<FinancialTransaction> findByApartmentIdOrderByTransactionDateDesc(UUID apartmentId);

    /**
     * Pobiera transakcje finansowe dla wskazanego lokalu, których data transakcji jest nie
     * wcześniejsza niż podana data graniczna. Używane w widoku mieszkańca do ograniczenia historii
     * do ostatnich 24 miesięcy zgodnie z wymaganiem WF-08.
     *
     * @param apartmentId identyfikator lokalu
     * @param cutoff najstarsza dopuszczalna data transakcji (włącznie)
     * @return lista transakcji posortowana malejąco po dacie
     */
    @Query(
            "SELECT ft FROM FinancialTransaction ft "
                    + "WHERE ft.apartment.id = :apartmentId "
                    + "AND ft.transactionDate >= :cutoff "
                    + "ORDER BY ft.transactionDate DESC")
    List<FinancialTransaction> findByApartmentIdAndTransactionDateAfter(
            @Param("apartmentId") UUID apartmentId, @Param("cutoff") LocalDate cutoff);
}

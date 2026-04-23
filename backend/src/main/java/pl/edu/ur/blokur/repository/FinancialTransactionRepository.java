package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.FinancialTransaction;

import java.util.List;
import java.util.UUID;

/**
 * Repozytorium JPA dla encji {@link FinancialTransaction}.
 * Udostępnia metody dostępu do historii transakcji finansowych lokalu.
 */
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    /**
     * Pobiera listę transakcji finansowych dla wskazanego lokalu,
     * posortowaną malejąco po dacie transakcji.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista transakcji posortowana od najnowszej
     */
    List<FinancialTransaction> findByApartmentIdOrderByTransactionDateDesc(UUID apartmentId);
}

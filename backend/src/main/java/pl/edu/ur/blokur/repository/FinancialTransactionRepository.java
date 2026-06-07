package pl.edu.ur.blokur.repository;

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
     * Zwraca mapę {apartmentId → data ostatniej wpłaty} dla podanej listy lokali. Zapytanie
     * grupujące eliminuje problem N+1 przy generowaniu zestawienia zaległości.
     *
     * @param apartmentIds lista identyfikatorów lokali
     * @return lista par [apartmentId, lastPaymentDate] jako {@code Object[]}
     */
    @Query(
            "SELECT ft.apartment.id, MAX(ft.transactionDate)"
                    + " FROM FinancialTransaction ft"
                    + " WHERE ft.type = 'WPLATA'"
                    + " AND ft.apartment.id IN :apartmentIds"
                    + " GROUP BY ft.apartment.id")
    List<Object[]> findLastPaymentDatesByApartmentIds(
            @Param("apartmentIds") List<UUID> apartmentIds);
}

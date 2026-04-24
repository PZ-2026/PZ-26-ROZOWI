package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.TicketCategory;

/** Repozytorium kategorii zgłoszeń serwisowych. */
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    /**
     * Zwraca kategorie, które nie zostały deaktywowane (soft delete).
     *
     * @return lista aktywnych kategorii
     */
    List<TicketCategory> findByIsActiveTrue();
}

package pl.edu.ur.blokur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.TicketCategory;

import java.util.List;
import java.util.UUID;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    List<TicketCategory> findByIsActiveTrue();
}

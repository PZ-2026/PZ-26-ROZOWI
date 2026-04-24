package pl.edu.ur.blokur.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.ur.blokur.models.TicketImage;

/** Repozytorium dla encji zdjęć zgłoszeń. */
public interface TicketImageRepository extends JpaRepository<TicketImage, UUID> {

    /**
     * Zwraca listę zdjęć dla danego zgłoszenia posortowaną po dacie dodania.
     *
     * @param ticketId identyfikator zgłoszenia
     * @return lista zdjęć
     */
    List<TicketImage> findByTicketIdOrderByUploadedAtAsc(UUID ticketId);
}

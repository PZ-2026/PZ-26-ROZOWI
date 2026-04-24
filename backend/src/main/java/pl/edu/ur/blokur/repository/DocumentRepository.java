package pl.edu.ur.blokur.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.ur.blokur.models.Document;

/** Repozytorium zarządzające encjami Document. */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}

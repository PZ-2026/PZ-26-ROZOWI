package pl.edu.ur.blokur.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.ur.blokur.models.Inspection;

/**
 * Repozytorium JPA dla encji {@link Inspection}. Udostępnia zapytania do filtrowania przeglądów po
 * zakresie użytkownika oraz wyszukiwania przeglądów zbliżających się w czasie.
 */
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    /**
     * Zwraca wszystkie przeglądy posortowane rosnąco według daty zaplanowania.
     *
     * @return lista wszystkich przeglądów
     */
    List<Inspection> findAllByOrderByScheduledAtAsc();

    /**
     * Zwraca przeglądy pasujące do hierarchii lokalizacyjnej użytkownika. Uwzględnia przeglądy na
     * poziomie klatki schodowej, budynku lub nieruchomości.
     *
     * @param staircaseId UUID klatki schodowej użytkownika (może być {@code null})
     * @param buildingId UUID budynku użytkownika (może być {@code null})
     * @param propertyId UUID nieruchomości użytkownika (może być {@code null})
     * @return lista pasujących przeglądów posortowana rosnąco po dacie
     */
    @Query(
            "SELECT i FROM Inspection i WHERE (i.scopeType ="
                + " pl.edu.ur.blokur.models.ScopeType.KLATKA AND i.scopeId = :staircaseId) OR"
                + " (i.scopeType = pl.edu.ur.blokur.models.ScopeType.BUDYNEK AND i.scopeId ="
                + " :buildingId) OR (i.scopeType = pl.edu.ur.blokur.models.ScopeType.NIERUCHOMOSC"
                + " AND i.scopeId = :propertyId) ORDER BY i.scheduledAt ASC")
    List<Inspection> findForUser(
            @Param("staircaseId") UUID staircaseId,
            @Param("buildingId") UUID buildingId,
            @Param("propertyId") UUID propertyId);

    /**
     * Zwraca przeglądy zaplanowane w podanym przedziale czasowym. Używane przez job przypomnień do
     * wykrywania inspekcji za 7 dni i 24 godziny.
     *
     * @param from początek przedziału (włącznie)
     * @param to koniec przedziału (wyłącznie)
     * @return lista przeglądów w przedziale posortowana rosnąco po dacie
     */
    @Query(
            "SELECT i FROM Inspection i WHERE i.scheduledAt >= :from AND i.scheduledAt < :to "
                    + "ORDER BY i.scheduledAt ASC")
    List<Inspection> findByScheduledAtBetween(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

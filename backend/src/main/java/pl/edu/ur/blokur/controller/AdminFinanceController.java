package pl.edu.ur.blokur.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.service.ApartmentBalanceService;

/**
 * Kontroler REST udostępniający zarządcy narzędzia do monitorowania sald i zaległości lokali.
 * Dostęp wyłącznie dla roli ZARZADCA.
 */
@RestController
@RequestMapping("/api/admin/apartments")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminFinanceController {

    private final ApartmentBalanceService apartmentBalanceService;

    /**
     * Tworzy kontroler z wymaganą zależnością.
     *
     * @param apartmentBalanceService serwis zestawienia sald i zaległości
     */
    public AdminFinanceController(ApartmentBalanceService apartmentBalanceService) {
        this.apartmentBalanceService = apartmentBalanceService;
    }

    /**
     * Zwraca zestawienie sald wszystkich lokali z możliwością filtrowania i sortowania. Przeznaczony
     * do widoku „Monitorowanie zaległości" w panelu zarządcy.
     *
     * <p>Parametry filtrowania:
     * <ul>
     *   <li>{@code propertyId} — zawęża wyniki do wybranej nieruchomości
     *   <li>{@code minDebt} — pokazuje lokale z zaległością ≥ podanej kwoty (PLN)
     *   <li>{@code minDaysOverdue} — pokazuje lokale zalegające ≥ podaną liczbę dni od ostatniej
     *       wpłaty
     *   <li>{@code sort} — {@code debt_asc} lub {@code debt_desc} (domyślnie: {@code debt_desc})
     * </ul>
     *
     * @param propertyId identyfikator nieruchomości (opcjonalny)
     * @param minDebt minimalna kwota zaległości w PLN (opcjonalny)
     * @param minDaysOverdue minimalna liczba dni zalegania (opcjonalny)
     * @param sort kierunek sortowania po kwocie zadłużenia (opcjonalny)
     * @return lista sald lokali spełniających kryteria, posortowana malejąco po zaległości
     */
    @GetMapping("/balances")
    public ResponseEntity<List<ApartmentBalanceResponse>> getApartmentBalances(
            @RequestParam(required = false) UUID propertyId,
            @RequestParam(required = false) BigDecimal minDebt,
            @RequestParam(required = false) Long minDaysOverdue,
            @RequestParam(required = false, defaultValue = "debt_desc") String sort) {
        boolean sortDesc = !"debt_asc".equalsIgnoreCase(sort);
        List<ApartmentBalanceResponse> result =
                apartmentBalanceService.getBalances(propertyId, minDebt, minDaysOverdue, sortDesc);
        return ResponseEntity.ok(result);
    }
}

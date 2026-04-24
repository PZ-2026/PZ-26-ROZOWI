package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.ApartmentTransactionsResponse;
import pl.edu.ur.blokur.dto.FinancialTransactionRequest;
import pl.edu.ur.blokur.dto.FinancialTransactionResponse;
import pl.edu.ur.blokur.service.FinancialTransactionService;

/**
 * Kontroler REST obsługujący operacje na transakcjach finansowych lokali. Udostępnia endpointy do
 * pobierania historii transakcji z saldem oraz do rejestrowania nowych operacji finansowych.
 */
@RestController
@RequestMapping("/api")
public class FinancialTransactionController {

    private final FinancialTransactionService financialTransactionService;

    public FinancialTransactionController(FinancialTransactionService financialTransactionService) {
        this.financialTransactionService = financialTransactionService;
    }

    /**
     * Pobiera historię transakcji finansowych oraz zbuforowane saldo dla wskazanego lokalu. Dostęp:
     * ZARZADCA, MIESZKANIEC.
     *
     * @param apartmentId identyfikator lokalu
     * @return historia transakcji z saldem (HTTP 200)
     */
    @GetMapping("/apartments/{apartmentId}/transactions")
    @PreAuthorize("hasAnyRole('ZARZADCA', 'MIESZKANIEC')")
    public ResponseEntity<ApartmentTransactionsResponse> getTransactions(
            @PathVariable UUID apartmentId) {
        ApartmentTransactionsResponse response =
                financialTransactionService.getTransactionsForApartment(apartmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Rejestruje nową transakcję finansową dla wskazanego lokalu. Operacja atomowo aktualizuje
     * saldo lokalu ({@code currentBalance}). Dostęp: ZARZADCA.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowej transakcji
     * @param principal kontekst bezpieczeństwa zalogowanego użytkownika
     * @return utworzona transakcja z kodem HTTP 201
     */
    @PostMapping("/apartments/{apartmentId}/transactions")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<FinancialTransactionResponse> createTransaction(
            @PathVariable UUID apartmentId,
            @Valid @RequestBody FinancialTransactionRequest request,
            Principal principal) {
        FinancialTransactionResponse response =
                financialTransactionService.createTransaction(
                        apartmentId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

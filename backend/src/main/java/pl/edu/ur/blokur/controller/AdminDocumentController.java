package pl.edu.ur.blokur.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.AnnualSettlementDistributionRequest;
import pl.edu.ur.blokur.dto.DocumentDistributionResult;
import pl.edu.ur.blokur.dto.RateChangeDistributionRequest;
import pl.edu.ur.blokur.service.DocumentDistributionService;

/**
 * Kontroler administracyjny do masowej dystrybucji dokumentów PDF dla mieszkańców. Dostęp
 * wyłącznie dla zarządcy (rola ZARZADCA).
 */
@RestController
@RequestMapping("/api/admin/documents")
@PreAuthorize("hasRole('ZARZADCA')")
public class AdminDocumentController {

    private final DocumentDistributionService documentDistributionService;

    /**
     * Tworzy kontroler z wymaganym serwisem.
     *
     * @param documentDistributionService serwis dystrybucji dokumentów
     */
    public AdminDocumentController(DocumentDistributionService documentDistributionService) {
        this.documentDistributionService = documentDistributionService;
    }

    /**
     * Generuje zawiadomienie o zmianie stawek opłat i dystrybuuje je do wybranych mieszkańców.
     * Każdy adresat dostaje osobny wpis dokumentu i powiadomienie PUSH.
     *
     * @param request dane zawiadomienia (tytuł, treść, data wejścia w życie, zakres odbiorców)
     * @param principal zalogowany zarządca
     * @return wynik dystrybucji (liczba dokumentów, liczba powiadomień)
     */
    @PostMapping("/rate-change")
    public ResponseEntity<DocumentDistributionResult> distributeRateChange(
            @Valid @RequestBody RateChangeDistributionRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        DocumentDistributionResult result =
                documentDistributionService.distributeRateChange(request, principal.getName());
        return ResponseEntity.ok(result);
    }

    /**
     * Generuje roczne rozliczenia kosztów lokali i dystrybuuje je do wybranych mieszkańców.
     * Rozliczenie zawiera zestawienie transakcji za wskazany rok, salda otwarcia i zamknięcia.
     *
     * @param request dane rozliczenia (rok, uwagi, zakres odbiorców)
     * @param principal zalogowany zarządca
     * @return wynik dystrybucji (liczba dokumentów, liczba powiadomień)
     */
    @PostMapping("/annual-settlement")
    public ResponseEntity<DocumentDistributionResult> distributeAnnualSettlement(
            @Valid @RequestBody AnnualSettlementDistributionRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        DocumentDistributionResult result =
                documentDistributionService.distributeAnnualSettlement(request, principal.getName());
        return ResponseEntity.ok(result);
    }
}

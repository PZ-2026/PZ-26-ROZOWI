package pl.edu.ur.blokur.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.ApartmentBalanceService;
import pl.edu.ur.blokur.service.DocumentService;
import pl.edu.ur.blokur.service.PdfGeneratorService;

/** Kontroler REST do generowania dokumentów PDF. */
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfGeneratorService pdfGeneratorService;
    private final ApartmentBalanceService apartmentBalanceService;
    private final DocumentService documentService;
    private final UserRepository userRepository;

    /**
     * Tworzy kontroler z wymaganymi zależnościami.
     *
     * @param pdfGeneratorService serwis generowania PDF
     * @param apartmentBalanceService serwis sald i zaległości lokali
     * @param documentService serwis archiwizacji wygenerowanych dokumentów
     * @param userRepository repozytorium użytkowników (do ustalenia owner przy archiwizacji)
     */
    public PdfController(
        PdfGeneratorService pdfGeneratorService,
        ApartmentBalanceService apartmentBalanceService,
        DocumentService documentService,
        UserRepository userRepository
    ) {
        this.pdfGeneratorService = pdfGeneratorService;
        this.apartmentBalanceService = apartmentBalanceService;
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    /**
     * Generuje protokół odbioru prac w formacie PDF.
     *
     * @param request dane do wypełnienia protokołu
     * @return plik PDF jako tablica bajtów z nagłówkami HTTP
     */
    @PostMapping("/work-acceptance-protocol")
    public ResponseEntity<byte[]> generateWorkAcceptanceProtocol(
        @RequestBody WorkAcceptanceProtocolRequest request
    ) {
        byte[] pdfBytes = pdfGeneratorService.generateWorkAcceptanceProtocol(
            request
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
            ContentDisposition.inline()
                .filename("protokol-odbioru-prac.pdf")
                .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Generuje zestawienie sald i zaległości lokali jako plik PDF. Akceptuje te same filtry co
     * endpoint JSON ({@code GET /api/admin/apartments/balances}). Dostęp: ZARZADCA.
     *
     * <p>Gdy parametr {@code save=true} dokument zostaje dodatkowo zarchiwizowany — zapisany w
     * storage i powiązany z wpisem w tabeli {@code documents} jako typ {@code RAPORT_SALD}.
     *
     * @param propertyId identyfikator nieruchomości (opcjonalny)
     * @param minDebt minimalna kwota zaległości w PLN (opcjonalny)
     * @param minDaysOverdue minimalna liczba dni zalegania (opcjonalny)
     * @param sort kierunek sortowania (opcjonalny, domyślnie {@code debt_desc})
     * @param save gdy {@code true} archiwizuje wygenerowany PDF w bazie dokumentów
     * @return plik PDF z zestawieniem
     */
    @GetMapping("/balances/pdf")
    @PreAuthorize("hasRole('ZARZADCA')")
    public ResponseEntity<byte[]> generateBalancesReport(
        @RequestParam(required = false) UUID propertyId,
        @RequestParam(required = false) BigDecimal minDebt,
        @RequestParam(required = false) Long minDaysOverdue,
        @RequestParam(required = false, defaultValue = "debt_desc") String sort,
        @RequestParam(required = false, defaultValue = "false") boolean save
    ) {
        boolean sortDesc = !"debt_asc".equalsIgnoreCase(sort);
        List<ApartmentBalanceResponse> rows =
            apartmentBalanceService.getBalances(
                propertyId,
                minDebt,
                minDaysOverdue,
                sortDesc
            );
        byte[] pdfBytes = pdfGeneratorService.generateBalancesReport(rows);

        if (save) {
            Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
            userRepository
                .findByEmail(auth.getName())
                .ifPresent(owner ->
                    documentService.storeGeneratedDocument(
                        "RAPORT_SALD",
                        "Zestawienie sald i zaległości",
                        pdfBytes,
                        owner,
                        null,
                        null,
                        null
                    )
                );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
            ContentDisposition.inline()
                .filename("zestawienie-zaleglosci.pdf")
                .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}

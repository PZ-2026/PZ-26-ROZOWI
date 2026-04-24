package pl.edu.ur.blokur.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.service.PdfGeneratorService;

/** Kontroler REST do generowania dokumentów PDF. */
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfGeneratorService pdfGeneratorService;

    public PdfController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    /**
     * Generuje protokół odbioru prac w formacie PDF.
     *
     * @param request dane do wypełnienia protokołu
     * @return plik PDF jako tablica bajtów z nagłówkami HTTP
     */
    @PostMapping("/work-acceptance-protocol")
    public ResponseEntity<byte[]> generateWorkAcceptanceProtocol(
            @RequestBody WorkAcceptanceProtocolRequest request) {
        byte[] pdfBytes = pdfGeneratorService.generateWorkAcceptanceProtocol(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline().filename("protokol-odbioru-prac.pdf").build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}

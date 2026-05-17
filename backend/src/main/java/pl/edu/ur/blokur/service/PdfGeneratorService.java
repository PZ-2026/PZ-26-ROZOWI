package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.pdflib.PdfGenerator;
import pl.edu.ur.blokur.pdflib.template.BalancesReportTemplate;
import pl.edu.ur.blokur.pdflib.template.WorkAcceptanceProtocolTemplate;
import pl.edu.ur.blokur.pdflib.template.data.BalanceRow;
import pl.edu.ur.blokur.pdflib.template.data.BalancesReportData;
import pl.edu.ur.blokur.pdflib.template.data.WorkAcceptanceProtocolData;

/**
 * Adapter Springa nad biblioteką {@code pdf-lib}. Mapuje DTO z warstwy aplikacji na POJO
 * biblioteczne i deleguje generowanie do {@link PdfGenerator}.
 *
 * <p>Cała mechanika iText, czcionek i layoutu znajduje się w bibliotece — ta klasa jest
 * intencjonalnie cienka.
 */
@Service
public class PdfGeneratorService {

    private final PdfGenerator pdfGenerator = new PdfGenerator();

    /**
     * Generuje protokół odbioru prac w formacie PDF.
     *
     * @param request dane do wypełnienia protokołu
     * @return wygenerowany plik PDF jako tablica bajtów
     */
    public byte[] generateWorkAcceptanceProtocol(WorkAcceptanceProtocolRequest request) {
        WorkAcceptanceProtocolData data =
                new WorkAcceptanceProtocolData(
                        request.getTicketNumber(),
                        request.getWorkDescription(),
                        request.getMaintenanceWorkerName(),
                        request.getBeforeImagesPaths(),
                        request.getAfterImagesPaths());
        return pdfGenerator.generate(new WorkAcceptanceProtocolTemplate(data));
    }

    /**
     * Generuje zestawienie sald i zaległości lokali w formacie PDF.
     *
     * @param rows przefiltrowane i posortowane salda lokali
     * @return wygenerowany plik PDF jako tablica bajtów
     */
    public byte[] generateBalancesReport(List<ApartmentBalanceResponse> rows) {
        List<BalanceRow> libRows =
                rows.stream()
                        .map(
                                r ->
                                        new BalanceRow(
                                                r.getAddress(),
                                                r.getBalance(),
                                                r.getLastPaymentDate(),
                                                r.getDaysOverdue()))
                        .collect(Collectors.toList());
        return pdfGenerator.generate(new BalancesReportTemplate(new BalancesReportData(libRows)));
    }
}

package pl.edu.ur.blokur.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;
import pl.edu.ur.blokur.pdflib.PdfGenerator;
import pl.edu.ur.blokur.pdflib.template.AnnualSettlementTemplate;
import pl.edu.ur.blokur.pdflib.template.BalancesReportTemplate;
import pl.edu.ur.blokur.pdflib.template.RateChangeNotificationTemplate;
import pl.edu.ur.blokur.pdflib.template.WorkAcceptanceProtocolTemplate;
import pl.edu.ur.blokur.pdflib.template.data.AnnualSettlementData;
import pl.edu.ur.blokur.pdflib.template.data.BalanceRow;
import pl.edu.ur.blokur.pdflib.template.data.BalancesReportData;
import pl.edu.ur.blokur.pdflib.template.data.RateChangeNotificationData;
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
     * Generuje zawiadomienie o zmianie stawek opłat w formacie PDF.
     *
     * @param subject tytuł zawiadomienia
     * @param body treść zawiadomienia
     * @param effectiveDate data wejścia w życie zmian
     * @param communityName nazwa wspólnoty
     * @return wygenerowany plik PDF jako tablica bajtów
     */
    public byte[] generateRateChangeNotification(
            String subject, String body, String effectiveDate, String communityName) {
        RateChangeNotificationData data =
                new RateChangeNotificationData(subject, body, effectiveDate, communityName);
        return pdfGenerator.generate(new RateChangeNotificationTemplate(data));
    }

    /**
     * Generuje roczne rozliczenie kosztów lokalu w formacie PDF.
     *
     * @param data dane rozliczenia (adres, rok, saldo, transakcje, uwagi)
     * @return wygenerowany plik PDF jako tablica bajtów
     */
    public byte[] generateAnnualSettlement(AnnualSettlementData data) {
        return pdfGenerator.generate(new AnnualSettlementTemplate(data));
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

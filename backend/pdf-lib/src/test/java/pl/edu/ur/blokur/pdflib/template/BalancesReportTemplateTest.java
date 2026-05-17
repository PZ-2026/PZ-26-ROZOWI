package pl.edu.ur.blokur.pdflib.template;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.ur.blokur.pdflib.PdfGenerator;
import pl.edu.ur.blokur.pdflib.template.data.BalanceRow;
import pl.edu.ur.blokur.pdflib.template.data.BalancesReportData;

/** Test szablonu raportu zestawienia sald i zaległości lokali. */
@DisplayName("BalancesReportTemplate — zestawienie sald")
class BalancesReportTemplateTest {

    private final PdfGenerator generator = new PdfGenerator();

    @Test
    @DisplayName("generuje raport z wieloma wierszami")
    void shouldGenerateReportWithRows() {
        List<BalanceRow> rows =
                List.of(
                        new BalanceRow(
                                "Słoneczna 5/12",
                                new BigDecimal("-450.00"),
                                LocalDate.of(2025, 11, 15),
                                30L),
                        new BalanceRow(
                                "Słoneczna 5/13",
                                new BigDecimal("200.50"),
                                LocalDate.of(2026, 1, 5),
                                null),
                        new BalanceRow("Słoneczna 5/14", BigDecimal.ZERO, null, null));

        byte[] pdf = generator.generate(new BalancesReportTemplate(new BalancesReportData(rows)));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertPdfHeader(pdf);
    }

    @Test
    @DisplayName("generuje raport z pustą listą — sama tabela nagłówków")
    void shouldGenerateEmptyReport() {
        byte[] pdf =
                generator.generate(
                        new BalancesReportTemplate(new BalancesReportData(List.of())));

        assertNotNull(pdf);
        assertTrue(pdf.length > 200);
        assertPdfHeader(pdf);
    }

    @Test
    @DisplayName("akceptuje null jako listę wierszy")
    void shouldAcceptNullRows() {
        byte[] pdf =
                generator.generate(new BalancesReportTemplate(new BalancesReportData(null)));

        assertNotNull(pdf);
        assertTrue(pdf.length > 200);
    }

    private static void assertPdfHeader(byte[] pdf) {
        assertTrue(pdf[0] == 0x25 && pdf[1] == 0x50 && pdf[2] == 0x44 && pdf[3] == 0x46);
    }
}

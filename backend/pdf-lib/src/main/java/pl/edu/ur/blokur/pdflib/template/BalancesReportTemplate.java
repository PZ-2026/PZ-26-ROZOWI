package pl.edu.ur.blokur.pdflib.template;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import pl.edu.ur.blokur.pdflib.PdfTemplate;
import pl.edu.ur.blokur.pdflib.template.data.BalanceRow;
import pl.edu.ur.blokur.pdflib.template.data.BalancesReportData;

/** Szablon raportu zestawienia sald i zaległości lokali (PDF). */
public class BalancesReportTemplate implements PdfTemplate {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final BalancesReportData data;

    /**
     * Tworzy szablon raportu sald.
     *
     * @param data dane do wypełnienia raportu
     */
    public BalancesReportTemplate(BalancesReportData data) {
        this.data = data;
    }

    @Override
    public void render(Document document) {
        document.add(
                new Paragraph("BLOKUR")
                        .setBold()
                        .setFontSize(18f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(
                new Paragraph("Zestawienie sald i zaległości")
                        .setBold()
                        .setFontSize(14f)
                        .setTextAlignment(TextAlignment.CENTER));

        String today = LocalDate.now().format(DATE_FORMAT);
        document.add(
                new Paragraph("Data wygenerowania: " + today)
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.RIGHT));

        document.add(new Paragraph("\n"));

        Table table =
                new Table(UnitValue.createPercentArray(new float[] {30f, 16f, 20f, 16f}))
                        .useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("Adres lokalu").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Saldo (PLN)").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Ostatnia wpłata").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Dni zalegania").setBold()));

        for (BalanceRow row : data.getRows()) {
            table.addCell(new Cell().add(new Paragraph(nullSafe(row.getAddress()))));
            table.addCell(
                    new Cell()
                            .add(
                                    new Paragraph(
                                            row.getBalance() != null
                                                    ? row.getBalance().setScale(2).toPlainString()
                                                    : "—")));
            table.addCell(
                    new Cell()
                            .add(
                                    new Paragraph(
                                            row.getLastPaymentDate() != null
                                                    ? row.getLastPaymentDate().format(DATE_FORMAT)
                                                    : "—")));
            table.addCell(
                    new Cell()
                            .add(
                                    new Paragraph(
                                            row.getDaysOverdue() != null
                                                    ? row.getDaysOverdue().toString()
                                                    : "—")));
        }

        document.add(table);
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}

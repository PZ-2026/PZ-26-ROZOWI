package pl.edu.ur.blokur.pdflib.template;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import pl.edu.ur.blokur.pdflib.PdfTemplate;
import pl.edu.ur.blokur.pdflib.template.data.AnnualSettlementData;
import pl.edu.ur.blokur.pdflib.template.data.AnnualSettlementRow;

/** Szablon rocznego rozliczenia kosztów lokalu (PDF). */
public class AnnualSettlementTemplate implements PdfTemplate {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AnnualSettlementData data;

    /**
     * Tworzy szablon rozliczenia rocznego z podanymi danymi.
     *
     * @param data dane do wypełnienia dokumentu
     */
    public AnnualSettlementTemplate(AnnualSettlementData data) {
        this.data = data;
    }

    @Override
    public void render(Document document) {
        document.add(
                new Paragraph(safe(data.getCommunityName(), "BLOKUR"))
                        .setBold()
                        .setFontSize(18f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(
                new Paragraph("System zarządzania nieruchomościami")
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        document.add(
                new Paragraph("ROCZNE ROZLICZENIE KOSZTÓW LOKALU — ROK " + data.getYear())
                        .setBold()
                        .setFontSize(15f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        document.add(
                new Paragraph("Lokal: " + safe(data.getApartmentAddress(), "—"))
                        .setFontSize(12f)
                        .setBold());

        document.add(new Paragraph("\n"));

        Table summaryTable =
                new Table(UnitValue.createPercentArray(new float[] {50f, 50f}))
                        .useAllAvailableWidth();

        summaryTable.addCell(new Cell().add(new Paragraph("Saldo na początku roku").setBold()));
        summaryTable.addCell(
                new Cell()
                        .add(
                                new Paragraph(
                                        formatAmount(data.getOpeningBalance()))
                                        .setTextAlignment(TextAlignment.RIGHT)));

        summaryTable.addCell(new Cell().add(new Paragraph("Saldo na końcu roku").setBold()));
        summaryTable.addCell(
                new Cell()
                        .add(
                                new Paragraph(
                                        formatAmount(data.getClosingBalance()))
                                        .setTextAlignment(TextAlignment.RIGHT)));

        document.add(summaryTable);

        document.add(new Paragraph("\n"));

        document.add(
                new Paragraph("Zestawienie operacji w roku " + data.getYear())
                        .setBold()
                        .setFontSize(12f));

        document.add(new Paragraph("\n"));

        Table transTable =
                new Table(UnitValue.createPercentArray(new float[] {18f, 20f, 42f, 20f}))
                        .useAllAvailableWidth();

        transTable.addHeaderCell(new Cell().add(new Paragraph("Data").setBold()));
        transTable.addHeaderCell(new Cell().add(new Paragraph("Typ").setBold()));
        transTable.addHeaderCell(new Cell().add(new Paragraph("Opis").setBold()));
        transTable.addHeaderCell(
                new Cell()
                        .add(new Paragraph("Kwota (PLN)").setBold()
                                .setTextAlignment(TextAlignment.RIGHT)));

        if (data.getRows() == null || data.getRows().isEmpty()) {
            transTable.addCell(
                    new Cell(1, 4)
                            .add(
                                    new Paragraph("Brak operacji w wybranym roku.")
                                            .setItalic()
                                            .setTextAlignment(TextAlignment.CENTER)));
        } else {
            for (AnnualSettlementRow row : data.getRows()) {
                transTable.addCell(
                        new Cell()
                                .add(
                                        new Paragraph(
                                                row.getDate() != null
                                                        ? row.getDate().format(DATE_FORMAT)
                                                        : "—")));
                transTable.addCell(new Cell().add(new Paragraph(safe(row.getType(), "—"))));
                transTable.addCell(
                        new Cell().add(new Paragraph(safe(row.getDescription(), ""))));
                transTable.addCell(
                        new Cell()
                                .add(
                                        new Paragraph(formatAmount(row.getAmount()))
                                                .setTextAlignment(TextAlignment.RIGHT)));
            }
        }

        document.add(transTable);

        if (data.getNote() != null && !data.getNote().isBlank()) {
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Uwagi zarządcy:").setBold().setFontSize(11f));
            document.add(new Paragraph(data.getNote()).setFontSize(11f));
        }

        document.add(new Paragraph("\n\n"));

        document.add(
                new Paragraph(
                        "Niniejsze rozliczenie zostało wygenerowane automatycznie przez system BlokUR.")
                        .setFontSize(9f)
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER));
    }

    private static String safe(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "—";
        return amount.setScale(2).toPlainString() + " zł";
    }
}

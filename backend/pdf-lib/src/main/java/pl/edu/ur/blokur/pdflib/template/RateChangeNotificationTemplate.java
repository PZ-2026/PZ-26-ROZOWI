package pl.edu.ur.blokur.pdflib.template;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import pl.edu.ur.blokur.pdflib.PdfTemplate;
import pl.edu.ur.blokur.pdflib.template.data.RateChangeNotificationData;

/** Szablon zawiadomienia o zmianie stawek opłat (PDF). */
public class RateChangeNotificationTemplate implements PdfTemplate {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final RateChangeNotificationData data;

    /**
     * Tworzy szablon zawiadomienia z podanymi danymi.
     *
     * @param data dane do wypełnienia dokumentu
     */
    public RateChangeNotificationTemplate(RateChangeNotificationData data) {
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
                new Paragraph("ZAWIADOMIENIE O ZMIANIE STAWEK OPŁAT")
                        .setBold()
                        .setFontSize(16f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        String today = LocalDate.now().format(DATE_FORMAT);
        document.add(
                new Paragraph("Data wystawienia: " + today)
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.RIGHT));

        document.add(new Paragraph("\n"));

        document.add(
                new Paragraph(safe(data.getSubject(), ""))
                        .setBold()
                        .setFontSize(14f));

        document.add(new Paragraph("\n"));

        if (data.getEffectiveDate() != null && !data.getEffectiveDate().isBlank()) {
            document.add(
                    new Paragraph("Data wejścia w życie: " + data.getEffectiveDate())
                            .setFontSize(11f)
                            .setBold());
            document.add(new Paragraph("\n"));
        }

        document.add(
                new Paragraph(safe(data.getBody(), ""))
                        .setFontSize(11f));

        document.add(new Paragraph("\n\n"));

        document.add(
                new Paragraph(
                        "Niniejsze zawiadomienie zostało wygenerowane automatycznie przez system BlokUR.")
                        .setFontSize(9f)
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER));
    }

    private static String safe(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}

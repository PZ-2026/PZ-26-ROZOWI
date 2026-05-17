package pl.edu.ur.blokur.pdflib.template;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import pl.edu.ur.blokur.pdflib.PdfTemplate;
import pl.edu.ur.blokur.pdflib.template.data.WorkAcceptanceProtocolData;

/**
 * Szablon protokołu odbioru prac konserwatorskich. Zawiera nagłówek wspólnoty, dane zgłoszenia,
 * opis wykonanych prac, opcjonalne zdjęcia przed/po oraz miejsce na podpisy.
 */
public class WorkAcceptanceProtocolTemplate implements PdfTemplate {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final WorkAcceptanceProtocolData data;

    /**
     * Tworzy szablon protokołu z podanymi danymi.
     *
     * @param data dane do wypełnienia protokołu
     */
    public WorkAcceptanceProtocolTemplate(WorkAcceptanceProtocolData data) {
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
                new Paragraph("Dane wspólnoty / zarządcy")
                        .setFontSize(11f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(
                new Paragraph("Miejsce na logo wspólnoty")
                        .setItalic()
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        document.add(
                new Paragraph("PROTOKÓŁ ODBIORU PRAC")
                        .setBold()
                        .setFontSize(16f)
                        .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        String currentDate = LocalDate.now().format(DATE_FORMAT);
        document.add(
                new Paragraph("Data wygenerowania dokumentu: " + currentDate).setFontSize(11f));

        document.add(new Paragraph("\n"));

        Table table =
                new Table(UnitValue.createPercentArray(new float[] {30f, 70f}))
                        .useAllAvailableWidth();

        table.addCell(new Cell().add(new Paragraph("Numer zgłoszenia")));
        table.addCell(new Cell().add(new Paragraph(safe(data.getTicketNumber()))));

        table.addCell(new Cell().add(new Paragraph("Imię konserwatora")));
        table.addCell(new Cell().add(new Paragraph(safe(data.getMaintenanceWorkerName()))));

        table.addCell(new Cell().add(new Paragraph("Opis wykonanych prac")));
        table.addCell(new Cell().add(new Paragraph(safe(data.getWorkDescription()))));

        document.add(table);

        document.add(new Paragraph("\n\n"));

        addImagesSection(document, "Zdjęcia przed naprawą", data.getBeforeImagesPaths());
        addImagesSection(document, "Zdjęcia po naprawie", data.getAfterImagesPaths());

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Podpis konserwatora: ______________________________"));
        document.add(new Paragraph("\n"));
        document.add(
                new Paragraph(
                        "Podpis zarządcy / osoby odbierającej: ______________________________"));
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private void addImagesSection(Document document, String title, List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        document.add(new Paragraph(title).setBold().setFontSize(14f));
        for (String path : imagePaths) {
            try {
                if (Files.exists(Paths.get(path))) {
                    ImageData imageData = ImageDataFactory.create(path);
                    Image img = new Image(imageData);
                    img.setAutoScale(true);
                    document.add(img);
                    document.add(new Paragraph("\n"));
                }
            } catch (Exception e) {
                // Brakujący lub uszkodzony obraz nie powinien przerywać generowania całego PDF.
                System.err.println(
                        "Nie udało się załadować obrazu z " + path + ": " + e.getMessage());
            }
        }
    }
}

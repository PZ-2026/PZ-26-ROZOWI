package pl.edu.ur.blokur.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.dto.WorkAcceptanceProtocolRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Serwis do generowania dokumentów PDF przy użyciu biblioteki iText7.
 * Używa czcionki NotoSans dla poprawnej obsługi polskich znaków (Unicode).
 */
@Service
public class PdfGeneratorService {

    /**
     * Generuje protokół odbioru prac w formacie PDF.
     *
     * @param request dane do wypełnienia protokołu
     * @return wygenerowany plik PDF jako tablica bajtów
     * @throws RuntimeException w przypadku błędu generowania PDF
     */
    public byte[] generateWorkAcceptanceProtocol(WorkAcceptanceProtocolRequest request) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        PdfFont font = loadUnicodeFont();
        document.setFont(font);

        document.add(new Paragraph("BLOKUR")
            .setBold()
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Dane wspólnoty / zarządcy")
            .setFontSize(11f)
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Miejsce na logo wspólnoty")
            .setItalic()
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("PROTOKÓŁ ODBIORU PRAC")
            .setBold()
            .setFontSize(16f)
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        document.add(new Paragraph("Data wygenerowania dokumentu: " + currentDate)
            .setFontSize(11f));

        document.add(new Paragraph("\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{30f, 70f}))
            .useAllAvailableWidth();

        table.addCell(new Cell().add(new Paragraph("Numer zgłoszenia")));
        table.addCell(new Cell().add(new Paragraph(request.getTicketNumber())));

        table.addCell(new Cell().add(new Paragraph("Imię konserwatora")));
        table.addCell(new Cell().add(new Paragraph(request.getMaintenanceWorkerName())));

        table.addCell(new Cell().add(new Paragraph("Opis wykonanych prac")));
        table.addCell(new Cell().add(new Paragraph(request.getWorkDescription())));

        document.add(table);

        document.add(new Paragraph("\n\n"));

        document.add(new Paragraph("Podpis konserwatora: ______________________________"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Podpis zarządcy / osoby odbierającej: ______________________________"));

        document.close();

        return outputStream.toByteArray();
    }

    /**
     * Wczytuje czcionkę NotoSans z zasobów aplikacji.
     * Czcionka obsługuje pełen zakres Unicode (w tym polskie znaki).
     *
     * @return obiekt {@link PdfFont} z wbudowaną czcionką
     * @throws RuntimeException jeśli plik czcionki nie zostanie znaleziony lub nie da się go wczytać
     */
    private PdfFont loadUnicodeFont() {
        try {
            ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            InputStream inputStream = resource.getInputStream();
            byte[] fontBytes = inputStream.readAllBytes();
            inputStream.close();

            return PdfFontFactory.createFont(
                fontBytes,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
        } catch (IOException e) {
            throw new RuntimeException("Nie można wczytać czcionki NotoSans", e);
        }
    }
}

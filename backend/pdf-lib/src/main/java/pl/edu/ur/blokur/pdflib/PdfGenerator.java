package pl.edu.ur.blokur.pdflib;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import java.io.ByteArrayOutputStream;
import pl.edu.ur.blokur.pdflib.exception.PdfGenerationException;
import pl.edu.ur.blokur.pdflib.font.UnicodeFontLoader;

/**
 * Fasada biblioteki generowania PDF. Otwiera i zamyka dokument iText, ładuje czcionkę Unicode
 * (NotoSans) i deleguje rysowanie zawartości do dostarczonego {@link PdfTemplate}.
 *
 * <p>Przykład użycia:
 *
 * <pre>{@code
 * PdfGenerator generator = new PdfGenerator();
 * byte[] pdf = generator.generate(new WorkAcceptanceProtocolTemplate(data));
 * }</pre>
 */
public class PdfGenerator {

    /**
     * Generuje dokument PDF na podstawie podanego szablonu.
     *
     * @param template szablon opisujący zawartość dokumentu
     * @return zawartość pliku PDF jako tablica bajtów
     * @throws PdfGenerationException w przypadku błędu generowania (np. brak czcionki, błąd I/O)
     */
    public byte[] generate(PdfTemplate template) {
        if (template == null) {
            throw new PdfGenerationException("Szablon PDF nie może być null");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument)) {

            PdfFont font = UnicodeFontLoader.loadDefault();
            document.setFont(font);

            template.render(document);

        } catch (PdfGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfGenerationException("Błąd generowania dokumentu PDF", e);
        }

        return out.toByteArray();
    }
}

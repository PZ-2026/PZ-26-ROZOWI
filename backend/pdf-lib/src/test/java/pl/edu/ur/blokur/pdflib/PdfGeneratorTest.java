package pl.edu.ur.blokur.pdflib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.layout.element.Paragraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.edu.ur.blokur.pdflib.exception.PdfGenerationException;

/** Testy fasady {@link PdfGenerator} — generowanie, walidacja magic bytes %PDF, obsługa błędów. */
@DisplayName("PdfGenerator — fasada biblioteki pdf-lib")
class PdfGeneratorTest {

    /** Pierwsze 4 bajty każdego prawidłowego pliku PDF: {@code %PDF}. */
    private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46};

    private final PdfGenerator generator = new PdfGenerator();

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("generuje niepusty PDF z poprawnym nagłówkiem magic bytes")
        void shouldGenerateValidPdf() {
            PdfTemplate template = document -> document.add(new Paragraph("Hello world"));

            byte[] pdf = generator.generate(template);

            assertNotNull(pdf);
            assertTrue(pdf.length > 200, "PDF powinien mieć minimalny rozmiar > 200B");

            byte[] header = new byte[4];
            System.arraycopy(pdf, 0, header, 0, 4);
            assertArrayEquals(
                    PDF_MAGIC_BYTES, header, "PDF musi zaczynać się od magic bytes %PDF");
        }

        @Test
        @DisplayName("obsługuje polskie znaki diakrytyczne (NotoSans, embedded font)")
        void shouldHandlePolishCharacters() {
            PdfTemplate template =
                    document -> document.add(new Paragraph("Zażółć gęślą jaźń ąćęłńóśźż"));

            byte[] pdf = generator.generate(template);

            assertNotNull(pdf);
            assertTrue(pdf.length > 200);
        }

        @Test
        @DisplayName("rzuca PdfGenerationException przy null szablonie")
        void shouldThrowOnNullTemplate() {
            assertThrows(PdfGenerationException.class, () -> generator.generate(null));
        }
    }
}

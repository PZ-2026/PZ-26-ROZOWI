package pl.edu.ur.blokur.pdflib.template;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.ur.blokur.pdflib.PdfGenerator;
import pl.edu.ur.blokur.pdflib.template.data.WorkAcceptanceProtocolData;

/** Test szablonu protokołu odbioru prac konserwatorskich. */
@DisplayName("WorkAcceptanceProtocolTemplate — protokół odbioru prac")
class WorkAcceptanceProtocolTemplateTest {

    private final PdfGenerator generator = new PdfGenerator();

    @Test
    @DisplayName("generuje protokół z pełnymi danymi i pustymi listami zdjęć")
    void shouldGenerateProtocol() {
        WorkAcceptanceProtocolData data =
                new WorkAcceptanceProtocolData(
                        "ZGL-2026-0001",
                        "Wymiana uszczelek w zaworach",
                        "Jan Kowalski");

        byte[] pdf = generator.generate(new WorkAcceptanceProtocolTemplate(data));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertPdfHeader(pdf);
    }

    @Test
    @DisplayName("toleruje brakujące ścieżki obrazów (nie przerywa generowania)")
    void shouldTolerateMissingImagePaths() {
        WorkAcceptanceProtocolData data =
                new WorkAcceptanceProtocolData(
                        "ZGL-2026-0002",
                        "Konserwacja",
                        "Anna Nowak",
                        List.of("/nieistnieje/before.jpg"),
                        List.of("/nieistnieje/after.jpg"));

        byte[] pdf = generator.generate(new WorkAcceptanceProtocolTemplate(data));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
    }

    @Test
    @DisplayName("toleruje null tytuł i opis (zastępuje pustym stringiem)")
    void shouldHandleNullFields() {
        WorkAcceptanceProtocolData data =
                new WorkAcceptanceProtocolData(null, null, null);

        byte[] pdf = generator.generate(new WorkAcceptanceProtocolTemplate(data));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
    }

    private static void assertPdfHeader(byte[] pdf) {
        assertTrue(pdf[0] == 0x25 && pdf[1] == 0x50 && pdf[2] == 0x44 && pdf[3] == 0x46,
                "PDF musi zaczynać się od %PDF");
    }
}

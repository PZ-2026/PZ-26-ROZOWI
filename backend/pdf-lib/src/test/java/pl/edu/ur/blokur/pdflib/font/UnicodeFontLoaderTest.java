package pl.edu.ur.blokur.pdflib.font;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.itextpdf.kernel.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.ur.blokur.pdflib.exception.PdfGenerationException;

/** Testy loadera czcionki — czytanie z classpath i obsługa brakujących plików. */
@DisplayName("UnicodeFontLoader — ładowanie czcionki NotoSans")
class UnicodeFontLoaderTest {

    @Test
    @DisplayName("ładuje domyślną czcionkę NotoSans z classpath")
    void shouldLoadDefaultFont() {
        PdfFont font = UnicodeFontLoader.loadDefault();

        assertNotNull(font);
        assertNotNull(font.getFontProgram());
    }

    @Test
    @DisplayName("rzuca PdfGenerationException przy nieistniejącej ścieżce")
    void shouldThrowOnMissingResource() {
        assertThrows(
                PdfGenerationException.class,
                () -> UnicodeFontLoader.load("fonts/nieistnieje.ttf"));
    }
}

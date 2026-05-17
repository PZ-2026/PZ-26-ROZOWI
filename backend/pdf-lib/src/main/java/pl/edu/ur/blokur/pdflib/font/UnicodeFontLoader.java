package pl.edu.ur.blokur.pdflib.font;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import java.io.IOException;
import java.io.InputStream;
import pl.edu.ur.blokur.pdflib.exception.PdfGenerationException;

/**
 * Loader czcionki Unicode (NotoSans) wbudowanej w bibliotekę. Czcionka obsługuje pełen zakres
 * Unicode, w tym polskie znaki diakrytyczne.
 */
public final class UnicodeFontLoader {

    private static final String DEFAULT_FONT_PATH = "fonts/NotoSans-Regular.ttf";

    private UnicodeFontLoader() {}

    /**
     * Ładuje domyślną czcionkę Unicode z classpath biblioteki.
     *
     * @return obiekt {@link PdfFont} gotowy do użycia w dokumencie iText
     * @throws PdfGenerationException jeśli plik czcionki nie zostanie znaleziony lub nie da się go
     *     wczytać
     */
    public static PdfFont loadDefault() {
        return load(DEFAULT_FONT_PATH);
    }

    /**
     * Ładuje czcionkę z podanej ścieżki w classpath.
     *
     * @param classpathResource ścieżka do pliku TTF w classpath (bez wiodącego ukośnika)
     * @return obiekt {@link PdfFont}
     * @throws PdfGenerationException jeśli plik czcionki nie istnieje lub jest uszkodzony
     */
    public static PdfFont load(String classpathResource) {
        try (InputStream is =
                UnicodeFontLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new PdfGenerationException(
                        "Nie znaleziono czcionki na classpath: " + classpathResource);
            }
            byte[] fontBytes = is.readAllBytes();
            return PdfFontFactory.createFont(
                    fontBytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (IOException e) {
            throw new PdfGenerationException("Nie można wczytać czcionki: " + classpathResource, e);
        }
    }
}

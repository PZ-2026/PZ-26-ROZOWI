package pl.edu.ur.blokur.pdflib.exception;

/** Wyjątek rzucany w przypadku niepowodzenia generowania dokumentu PDF. */
public class PdfGenerationException extends RuntimeException {

    /**
     * Tworzy wyjątek z komunikatem.
     *
     * @param message komunikat błędu
     */
    public PdfGenerationException(String message) {
        super(message);
    }

    /**
     * Tworzy wyjątek z komunikatem i przyczyną.
     *
     * @param message komunikat błędu
     * @param cause przyczyna źródłowa
     */
    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

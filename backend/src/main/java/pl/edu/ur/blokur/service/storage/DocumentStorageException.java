package pl.edu.ur.blokur.service.storage;

/** Wyjątek sygnalizujący błąd operacji w warstwie {@link DocumentStorage}. */
public class DocumentStorageException extends RuntimeException {

    /**
     * Tworzy wyjątek z komunikatem.
     *
     * @param message komunikat błędu
     */
    public DocumentStorageException(String message) {
        super(message);
    }

    /**
     * Tworzy wyjątek z komunikatem i przyczyną.
     *
     * @param message komunikat błędu
     * @param cause przyczyna źródłowa
     */
    public DocumentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

package pl.edu.ur.blokur.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.ur.blokur.pdflib.exception.PdfGenerationException;

/**
 * Globalny handler wyjątków — mapuje wyjątki domenowe na odpowiednie kody HTTP. Bez tego handlera
 * Spring zwróciłby 500 dla każdego nieobsłużonego RuntimeException.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Obsługuje naruszenia reguł biznesowych (np. niepoprawny status zgłoszenia). Zwraca 422
     * Unprocessable Entity z komunikatem błędu.
     *
     * @param e wyjątek biznesowy
     * @return odpowiedź 422 z komunikatem
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, String>> handleBusinessValidation(
            BusinessValidationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * Obsługuje błędy zasobu nieznalezionego. Zwraca 404 Not Found.
     *
     * @param e wyjątek not found
     * @return odpowiedź 404 z komunikatem
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * Obsługuje błędy generowania PDF (np. brak czcionki, błąd iText). Zwraca 500 Internal Server
     * Error z czytelnym komunikatem zamiast stacktrace'a.
     *
     * @param e wyjątek generowania PDF
     * @return odpowiedź 500 z komunikatem
     */
    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<Map<String, String>> handlePdfGeneration(PdfGenerationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Błąd generowania dokumentu PDF: " + e.getMessage()));
    }

    /**
     * Obsługuje błędy zabezpieczeń (np. próba zamknięcia cudzego zgłoszenia). Zwraca 403
     * Forbidden.
     *
     * @param e wyjątek bezpieczeństwa
     * @return odpowiedź 403 z komunikatem
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurity(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }
}

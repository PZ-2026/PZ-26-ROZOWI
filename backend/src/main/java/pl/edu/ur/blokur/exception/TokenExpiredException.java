package pl.edu.ur.blokur.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Wyjątek rzucany gdy token (zaproszenia lub resetu hasła) wygasł. Skutkuje odpowiedzią HTTP 410
 * (Gone).
 */
@ResponseStatus(HttpStatus.GONE)
public class TokenExpiredException extends RuntimeException {

    /**
     * Tworzy wyjątek z podanym komunikatem.
     *
     * @param message opis przyczyny wygaśnięcia
     */
    public TokenExpiredException(String message) {
        super(message);
    }
}

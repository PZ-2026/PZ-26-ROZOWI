package pl.edu.ur.blokur.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Wyjątek biznesowy rzucany w przypadku naruszenia reguł domenowych. Skutkuje odpowiedzią HTTP 422
 * (Unprocessable Entity).
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}

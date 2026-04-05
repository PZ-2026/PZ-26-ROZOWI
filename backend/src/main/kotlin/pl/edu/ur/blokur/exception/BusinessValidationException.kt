package pl.edu.ur.blokur.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class BusinessValidationException(message: String) : RuntimeException(message)

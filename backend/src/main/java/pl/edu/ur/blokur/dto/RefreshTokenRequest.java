package pl.edu.ur.blokur.dto;

import lombok.Data;

/** DTO z wartością refresh tokenu wymienianego na nową parę tokenów. */
@Data
public class RefreshTokenRequest {

    private String refreshToken;
}

package pl.edu.ur.blokur.dto;

import lombok.Data;

/** DTO z odpowiedzią po pomyślnym logowaniu — zawiera token JWT i rolę użytkownika. */
@Data
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String role;

    /**
     * Tworzy odpowiedź uwierzytelniania z parą tokenów i rolą użytkownika.
     *
     * @param token krótkotrwały access token JWT
     * @param refreshToken długotrwały refresh token do odnawiania sesji
     * @param role rola zalogowanego użytkownika
     */
    public AuthResponse(String token, String refreshToken, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
    }
}

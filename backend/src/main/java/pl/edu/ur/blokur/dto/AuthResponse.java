package pl.edu.ur.blokur.dto;

/** DTO z odpowiedzią po pomyślnym logowaniu — zawiera token JWT i rolę użytkownika. */
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

    /**
     * Zwraca access token JWT.
     *
     * @return access token JWT
     */
    public String getToken() {
        return token;
    }

    /**
     * Ustawia access token JWT.
     *
     * @param token access token JWT
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Zwraca refresh token służący do odnawiania sesji.
     *
     * @return refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Ustawia refresh token.
     *
     * @param refreshToken refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Zwraca rolę zalogowanego użytkownika.
     *
     * @return rola użytkownika (np. MIESZKANIEC, ZARZADCA)
     */
    public String getRole() {
        return role;
    }

    /**
     * Ustawia rolę użytkownika.
     *
     * @param role rola użytkownika
     */
    public void setRole(String role) {
        this.role = role;
    }
}

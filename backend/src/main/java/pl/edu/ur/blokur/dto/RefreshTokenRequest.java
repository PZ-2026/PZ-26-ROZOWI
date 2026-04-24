package pl.edu.ur.blokur.dto;

/** DTO z wartością refresh tokenu wymienianego na nową parę tokenów. */
public class RefreshTokenRequest {

    private String refreshToken;

    /** Konstruktor bezargumentowy wymagany przez deserializację Jacksona. */
    public RefreshTokenRequest() {}

    /**
     * Zwraca wartość refresh tokenu przesłaną przez klienta.
     *
     * @return wartość refresh tokenu
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Ustawia wartość refresh tokenu.
     *
     * @param refreshToken wartość refresh tokenu
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

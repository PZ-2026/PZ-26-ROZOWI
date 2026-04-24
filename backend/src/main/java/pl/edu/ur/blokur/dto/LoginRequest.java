package pl.edu.ur.blokur.dto;

/** DTO z danymi logowania przesyłanymi przez użytkownika. */
public class LoginRequest {

    private String username = "";
    private String password = "";

    /** Konstruktor bezargumentowy wymagany przez deserializację Jacksona. */
    public LoginRequest() {}

    /**
     * Tworzy żądanie logowania z podanymi danymi uwierzytelniającymi.
     *
     * @param username adres e-mail użytkownika
     * @param password hasło użytkownika
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Zwraca nazwę użytkownika (adres e-mail).
     *
     * @return adres e-mail
     */
    public String getUsername() {
        return username;
    }

    /**
     * Ustawia nazwę użytkownika (adres e-mail).
     *
     * @param username adres e-mail
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Zwraca hasło użytkownika.
     *
     * @return hasło
     */
    public String getPassword() {
        return password;
    }

    /**
     * Ustawia hasło użytkownika.
     *
     * @param password hasło
     */
    public void setPassword(String password) {
        this.password = password;
    }
}

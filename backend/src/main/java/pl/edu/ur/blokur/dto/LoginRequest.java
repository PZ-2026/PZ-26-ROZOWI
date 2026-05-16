package pl.edu.ur.blokur.dto;

import lombok.Data;

/** DTO z danymi logowania przesyłanymi przez użytkownika. */
@Data
public class LoginRequest {

    private String username = "";
    private String password = "";

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
}

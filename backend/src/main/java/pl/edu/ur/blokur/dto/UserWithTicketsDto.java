package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO reprezentujące użytkownika wraz z liczbą jego aktywnych zgłoszeń. */
@Data
public class UserWithTicketsDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private long activeTicketsCount;

    /**
     * Tworzy DTO użytkownika z liczbą aktywnych zgłoszeń.
     *
     * @param id identyfikator użytkownika
     * @param firstName imię użytkownika
     * @param lastName nazwisko użytkownika
     * @param email adres e-mail użytkownika
     * @param phone numer telefonu użytkownika
     * @param activeTicketsCount liczba aktywnych zgłoszeń przypisanych do użytkownika
     */
    public UserWithTicketsDto(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            long activeTicketsCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.activeTicketsCount = activeTicketsCount;
    }
}

package pl.edu.ur.blokur.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** DTO z informacją o saldzie pojedynczego lokalu zwracane w zestawieniu zaległości. */
public class ApartmentBalanceResponse {

    private final UUID apartmentId;
    private final String address;
    private final BigDecimal balance;
    private final LocalDate lastPaymentDate;
    private final Long daysOverdue;

    /**
     * Tworzy odpowiedź z danymi o saldzie lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param address adres lokalu (budynek + numer)
     * @param balance aktualne saldo (ujemne = zaległość)
     * @param lastPaymentDate data ostatniej wpłaty lub {@code null} jeśli brak
     * @param daysOverdue liczba dni od ostatniej wpłaty lub {@code null} jeśli brak wpłat
     */
    public ApartmentBalanceResponse(
            UUID apartmentId,
            String address,
            BigDecimal balance,
            LocalDate lastPaymentDate,
            Long daysOverdue) {
        this.apartmentId = apartmentId;
        this.address = address;
        this.balance = balance;
        this.lastPaymentDate = lastPaymentDate;
        this.daysOverdue = daysOverdue;
    }

    /**
     * Zwraca identyfikator lokalu.
     *
     * @return identyfikator UUID
     */
    public UUID getApartmentId() {
        return apartmentId;
    }

    /**
     * Zwraca adres lokalu złożony z adresu budynku i numeru lokalu.
     *
     * @return adres w formie czytelnej dla człowieka
     */
    public String getAddress() {
        return address;
    }

    /**
     * Zwraca aktualne saldo rozliczeniowe lokalu. Wartość ujemna oznacza zaległość.
     *
     * @return saldo w PLN
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Zwraca datę ostatniej zaksięgowanej wpłaty (typ WPLATA).
     *
     * @return data ostatniej wpłaty lub {@code null} jeśli nie zarejestrowano żadnej wpłaty
     */
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    /**
     * Zwraca liczbę dni od ostatniej wpłaty (liczba dni zalegania).
     *
     * @return dni zalegania lub {@code null} jeśli nie zarejestrowano żadnej wpłaty
     */
    public Long getDaysOverdue() {
        return daysOverdue;
    }
}

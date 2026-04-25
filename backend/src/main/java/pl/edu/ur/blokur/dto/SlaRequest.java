package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO wejściowe dla operacji ustawiania czasu reakcji SLA kategorii zgłoszenia.
 *
 * <p>Zarządca przekazuje {@code slaHours} — docelową liczbę godzin roboczych, w której zgłoszenie
 * powinno zostać obsłużone. Przekroczenie tej wartości powoduje oznaczenie zgłoszenia flagą {@code
 * sla_breached}.
 */
public class SlaRequest {

    @NotNull(message = "Liczba godzin SLA nie może być pusta")
    @Min(value = 1, message = "Liczba godzin SLA musi wynosić co najmniej 1")
    private Integer slaHours;

    /**
     * Zwraca docelowy czas reakcji w godzinach roboczych.
     *
     * @return liczba godzin roboczych SLA
     */
    public Integer getSlaHours() {
        return slaHours;
    }

    /**
     * Ustawia docelowy czas reakcji w godzinach roboczych.
     *
     * @param slaHours liczba godzin roboczych SLA (min. 1)
     */
    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }
}

package pl.edu.ur.blokur.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO wejściowe dla operacji ustawiania czasu reakcji SLA kategorii zgłoszenia.
 *
 * <p>Zarządca przekazuje {@code slaHours} — docelową liczbę godzin roboczych, w której zgłoszenie
 * powinno zostać obsłużone. Przekroczenie tej wartości powoduje oznaczenie zgłoszenia flagą {@code
 * sla_breached}.
 */
@Data
public class SlaRequest {

    @NotNull(message = "Liczba godzin SLA nie może być pusta")
    @Min(value = 1, message = "Liczba godzin SLA musi wynosić co najmniej 1")
    private Integer slaHours;
}

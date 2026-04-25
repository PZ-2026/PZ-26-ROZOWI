package pl.edu.ur.blokur.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kalkulator godzin roboczych używany przy weryfikacji przekroczenia SLA zgłoszeń.
 *
 * <p>Godziny robocze obejmują dni od poniedziałku do piątku w przedziale czasowym {@code
 * sla.work-start}:00–{@code sla.work-end}:00 (domyślnie 8:00–16:00). Wartości te są konfigurowalne
 * przez zmienne środowiskowe.
 */
@Component
public class BusinessHoursCalculator {

    private final int workStart;
    private final int workEnd;

    /**
     * Tworzy kalkulator z konfigurowalnymi godzinami roboczymi.
     *
     * @param workStart godzina rozpoczęcia dnia roboczego (np. 8 oznacza 08:00)
     * @param workEnd godzina zakończenia dnia roboczego (np. 16 oznacza 16:00)
     */
    public BusinessHoursCalculator(
            @Value("${sla.work-start:8}") int workStart, @Value("${sla.work-end:16}") int workEnd) {
        this.workStart = workStart;
        this.workEnd = workEnd;
    }

    /**
     * Oblicza liczbę godzin roboczych (pon–pt, {@code workStart}:00–{@code workEnd}:00) między
     * dwoma momentami w czasie.
     *
     * <p>Przykład: zgłoszenie złożone w piątek o 15:00 przy konfiguracji 8–16 — do poniedziałku
     * 9:00 zostanie naliczona 1 godzina (pt 15–16) + 1 godzina (pn 8–9) = 2 godziny.
     *
     * @param from moment początkowy (zazwyczaj {@code created_at} zgłoszenia)
     * @param to moment końcowy (zazwyczaj chwila bieżąca)
     * @return liczba godzin roboczych jako wartość zmiennoprzecinkowa; 0.0 gdy {@code from} lub
     *     {@code to} jest {@code null} albo {@code to} nie następuje po {@code from}
     */
    public double calculate(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || !to.isAfter(from)) {
            return 0.0;
        }

        double totalMinutes = 0;
        LocalDate date = from.toLocalDate();
        LocalDate endDate = to.toLocalDate();

        while (!date.isAfter(endDate)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                LocalDateTime dayStart = date.atTime(workStart, 0);
                LocalDateTime dayEnd = date.atTime(workEnd, 0);
                LocalDateTime effectiveStart = from.isAfter(dayStart) ? from : dayStart;
                LocalDateTime effectiveEnd = to.isBefore(dayEnd) ? to : dayEnd;
                if (effectiveEnd.isAfter(effectiveStart)) {
                    totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes();
                }
            }
            date = date.plusDays(1);
        }

        return totalMinutes / 60.0;
    }
}

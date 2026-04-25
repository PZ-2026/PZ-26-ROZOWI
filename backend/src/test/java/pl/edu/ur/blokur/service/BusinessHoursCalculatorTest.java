package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testy jednostkowe dla {@link BusinessHoursCalculator}. Weryfikują obliczanie godzin roboczych
 * (pon–pt 8:00–16:00) między dwoma momentami w czasie.
 */
@DisplayName("BusinessHoursCalculator — obliczanie godzin roboczych")
class BusinessHoursCalculatorTest {

    private BusinessHoursCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BusinessHoursCalculator(8, 16);
    }

    @Nested
    @DisplayName("przypadki brzegowe")
    class EdgeCases {

        @Test
        @DisplayName("zwraca 0.0 gdy from jest null")
        void returnsZeroWhenFromIsNull() {
            double result = calculator.calculate(null, LocalDateTime.of(2026, 4, 21, 10, 0));

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("zwraca 0.0 gdy to jest null")
        void returnsZeroWhenToIsNull() {
            double result = calculator.calculate(LocalDateTime.of(2026, 4, 21, 10, 0), null);

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("zwraca 0.0 gdy to nie następuje po from")
        void returnsZeroWhenToBeforeFrom() {
            LocalDateTime from = LocalDateTime.of(2026, 4, 21, 12, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 21, 10, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("zwraca 0.0 gdy from == to")
        void returnsZeroWhenFromEqualsTo() {
            LocalDateTime moment = LocalDateTime.of(2026, 4, 21, 10, 0);

            double result = calculator.calculate(moment, moment);

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("ten sam dzień roboczy")
    class SameWorkingDay {

        @Test
        @DisplayName("zlicza pełne 8 godzin roboczych w ciągu jednego dnia")
        void countsFullWorkingDay() {
            // poniedziałek 2026-04-20
            LocalDateTime from = LocalDateTime.of(2026, 4, 20, 8, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 20, 16, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(8.0, within(0.001));
        }

        @Test
        @DisplayName("zlicza częściowy czas — środek dnia roboczego")
        void countsPartialWorkingPeriod() {
            // wtorek 2026-04-21, 10:00–14:00 = 4 godziny
            LocalDateTime from = LocalDateTime.of(2026, 4, 21, 10, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 21, 14, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(4.0, within(0.001));
        }

        @Test
        @DisplayName("ogranicza czas do godziny startowej gdy from wcześniej niż 8:00")
        void clipsToWorkStartWhenFromBeforeWorkHours() {
            // środa 2026-04-22, 6:00–10:00, liczymy tylko 8:00–10:00 = 2 h
            LocalDateTime from = LocalDateTime.of(2026, 4, 22, 6, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 22, 10, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("ogranicza czas do godziny końcowej gdy to późniejsze niż 16:00")
        void clipsToWorkEndWhenToAfterWorkHours() {
            // czwartek 2026-04-23, 14:00–18:00, liczymy tylko 14:00–16:00 = 2 h
            LocalDateTime from = LocalDateTime.of(2026, 4, 23, 14, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 23, 18, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("zwraca 0.0 gdy cały zakres poza godzinami roboczymi")
        void returnsZeroWhenRangeOutsideWorkHours() {
            // piątek 2026-04-24, 17:00–19:00 — poza godzinami pracy
            LocalDateTime from = LocalDateTime.of(2026, 4, 24, 17, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 24, 19, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("weekend — sobota i niedziela")
    class Weekend {

        @Test
        @DisplayName("pomija sobotę — zwraca 0 godzin")
        void skipsSaturday() {
            // sobota 2026-04-18, 10:00–14:00
            LocalDateTime from = LocalDateTime.of(2026, 4, 18, 10, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 18, 14, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("pomija niedzielę — zwraca 0 godzin")
        void skipsSunday() {
            // niedziela 2026-04-19, 8:00–16:00
            LocalDateTime from = LocalDateTime.of(2026, 4, 19, 8, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 19, 16, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("pomija weekend przy obliczaniu przez tydzień roboczy")
        void skipsWeekendSpanningFullWeek() {
            // piątek 2026-04-24 8:00 — poniedziałek 2026-04-27 16:00
            // pt: 8h + pn: 8h = 16h (sb i nd pominięte)
            LocalDateTime from = LocalDateTime.of(2026, 4, 24, 8, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 27, 16, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(16.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("wiele dni roboczych")
    class MultipleWorkdays {

        @Test
        @DisplayName("zlicza godziny przez pełne 5 dni roboczych")
        void countsFullWorkweek() {
            // pon 2026-04-20 8:00 — pt 2026-04-24 16:00 = 5 × 8h = 40h
            LocalDateTime from = LocalDateTime.of(2026, 4, 20, 8, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 24, 16, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(40.0, within(0.001));
        }

        @Test
        @DisplayName("zlicza godziny przez dwa dni robocze z częściowymi granicami")
        void countsTwoDaysWithPartialBoundaries() {
            // piątek 2026-04-24 15:00 — poniedziałek 2026-04-27 9:00
            // pt: 15:00–16:00 = 1h; pn: 8:00–9:00 = 1h; razem = 2h
            LocalDateTime from = LocalDateTime.of(2026, 4, 24, 15, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 27, 9, 0);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("zlicza minuty — dokładność sub-godzinowa")
        void countsMinutesPrecision() {
            // wtorek 2026-04-21, 8:00–8:30 = 0.5 godziny
            LocalDateTime from = LocalDateTime.of(2026, 4, 21, 8, 0);
            LocalDateTime to = LocalDateTime.of(2026, 4, 21, 8, 30);

            double result = calculator.calculate(from, to);

            assertThat(result).isCloseTo(0.5, within(0.001));
        }
    }
}

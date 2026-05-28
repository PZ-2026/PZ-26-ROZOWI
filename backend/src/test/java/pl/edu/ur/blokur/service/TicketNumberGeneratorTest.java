package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TicketNumberGenerator — generowanie unikalnych numerów zgłoszeń")
class TicketNumberGeneratorTest {

    private TicketNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TicketNumberGenerator();
    }

    @Nested
    @DisplayName("generate — generowanie kolejnych numerów")
    class GenerateTests {

        @Test
        @DisplayName("Generuje numer w formacie ZGL-RRRR-NNNN")
        void shouldGenerateNumberInCorrectFormat() {
            String number = generator.generate();
            int currentYear = LocalDate.now().getYear();

            assertThat(number).matches("ZGL-\\d{4}-\\d{4}");
            assertThat(number).startsWith("ZGL-" + currentYear + "-");
        }

        @Test
        @DisplayName("Kolejne wywołania generują rosnące numery")
        void shouldGenerateIncreasingNumbers() {
            String first = generator.generate();
            String second = generator.generate();
            String third = generator.generate();

            int year = LocalDate.now().getYear();
            assertThat(first).isEqualTo(String.format("ZGL-%d-0001", year));
            assertThat(second).isEqualTo(String.format("ZGL-%d-0002", year));
            assertThat(third).isEqualTo(String.format("ZGL-%d-0003", year));
        }

        @Test
        @DisplayName("Numery są wypełniane zerami do 4 cyfr")
        void shouldPadNumberToFourDigits() {
            String number = generator.generate();

            String[] parts = number.split("-");
            assertThat(parts[2]).hasSize(4);
        }
    }

    @Nested
    @DisplayName("initYear — inicjalizacja licznika roku")
    class InitYearTests {

        @Test
        @DisplayName("Po initYear kolejny numer zaczyna od lastValue+1")
        void shouldGenerateFromInitializedValue() {
            int year = LocalDate.now().getYear();
            generator.initYear(year, 42);

            String next = generator.generate();

            assertThat(next).isEqualTo(String.format("ZGL-%d-0043", year));
        }

        @Test
        @DisplayName("initYear z wartością 0 powoduje że następny numer to 0001")
        void shouldStartFromOneWhenInitializedWithZero() {
            int year = LocalDate.now().getYear();
            generator.initYear(year, 0);

            String next = generator.generate();

            assertThat(next).isEqualTo(String.format("ZGL-%d-0001", year));
        }

        @Test
        @DisplayName("initYear nadpisuje istniejący licznik")
        void shouldOverwriteExistingCounter() {
            int year = LocalDate.now().getYear();
            generator.generate();
            generator.generate();
            generator.initYear(year, 100);

            String next = generator.generate();

            assertThat(next).isEqualTo(String.format("ZGL-%d-0101", year));
        }
    }
}

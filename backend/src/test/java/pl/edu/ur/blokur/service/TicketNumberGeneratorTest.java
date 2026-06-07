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
        @DisplayName("Generuje numer w formacie ZGL/RRRR/NNN")
        void shouldGenerateNumberInCorrectFormat() {
            String number = generator.generate();
            int currentYear = LocalDate.now().getYear();

            assertThat(number).matches("ZGL/\\d{4}/\\d{3}");
            assertThat(number).startsWith("ZGL/" + currentYear + "/");
        }

        @Test
        @DisplayName("Kolejne wywołania generują rosnące numery")
        void shouldGenerateIncreasingNumbers() {
            String first = generator.generate();
            String second = generator.generate();
            String third = generator.generate();

            int year = LocalDate.now().getYear();
            assertThat(first).isEqualTo(String.format("ZGL/%d/0001", year).replace("0001", "001"));
            assertThat(second).isEqualTo(String.format("ZGL/%d/0002", year).replace("0002", "002"));
            assertThat(third).isEqualTo(String.format("ZGL/%d/0003", year).replace("0003", "003"));
        }

        @Test
        @DisplayName("Numery są wypełniane zerami do 3 cyfr")
        void shouldPadNumberToThreeDigits() {
            String number = generator.generate();

            String[] parts = number.split("/");
            assertThat(parts[2]).hasSize(3);
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

            assertThat(next).isEqualTo(String.format("ZGL/%d/043", year));
        }

        @Test
        @DisplayName("initYear z wartością 0 powoduje że następny numer to 001")
        void shouldStartFromOneWhenInitializedWithZero() {
            int year = LocalDate.now().getYear();
            generator.initYear(year, 0);

            String next = generator.generate();

            assertThat(next).isEqualTo(String.format("ZGL/%d/001", year));
        }

        @Test
        @DisplayName("initYear nadpisuje istniejący licznik")
        void shouldOverwriteExistingCounter() {
            int year = LocalDate.now().getYear();
            generator.generate();
            generator.generate();
            generator.initYear(year, 100);

            String next = generator.generate();

            assertThat(next).isEqualTo(String.format("ZGL/%d/101", year));
        }
    }
}

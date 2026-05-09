package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.models.TicketStatus;

@DisplayName("TicketStateMachine")
class TicketStateMachineTest {

    private TicketStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new TicketStateMachine();
    }

    // =========================================================
    // validateTransition — dozwolone przejścia
    // =========================================================

    @Nested
    @DisplayName("validateTransition — dozwolone przejścia")
    class AllowedTransitions {

        @Test
        @DisplayName("NOWE → ZAPLANOWANO")
        void noweToZaplanowano() {
            stateMachine.validateTransition(TicketStatus.NOWE, TicketStatus.ZAPLANOWANO);
        }

        @Test
        @DisplayName("NOWE → ODRZUCONE")
        void noweToOdrzucone() {
            stateMachine.validateTransition(TicketStatus.NOWE, TicketStatus.ODRZUCONE);
        }

        @Test
        @DisplayName("ZAPLANOWANO → W_REALIZACJI")
        void zaplanowanoToWRealizacji() {
            stateMachine.validateTransition(TicketStatus.ZAPLANOWANO, TicketStatus.W_REALIZACJI);
        }

        @Test
        @DisplayName("ZAPLANOWANO → WSTRZYMANO")
        void zaplanowanoToWstrzymano() {
            stateMachine.validateTransition(TicketStatus.ZAPLANOWANO, TicketStatus.WSTRZYMANO);
        }

        @Test
        @DisplayName("ZAPLANOWANO → ODRZUCONE")
        void zaplanowanoToOdrzucone() {
            stateMachine.validateTransition(TicketStatus.ZAPLANOWANO, TicketStatus.ODRZUCONE);
        }

        @Test
        @DisplayName("W_REALIZACJI → WSTRZYMANO")
        void wRealizacjiToWstrzymano() {
            stateMachine.validateTransition(TicketStatus.W_REALIZACJI, TicketStatus.WSTRZYMANO);
        }

        @Test
        @DisplayName("W_REALIZACJI → ZAKONCZONE")
        void wRealizacjiToZakonczone() {
            stateMachine.validateTransition(
                    TicketStatus.W_REALIZACJI, TicketStatus.ZAKONCZONE);
        }

        @Test
        @DisplayName("WSTRZYMANO → W_REALIZACJI")
        void wstrzymanoToWRealizacji() {
            stateMachine.validateTransition(TicketStatus.WSTRZYMANO, TicketStatus.W_REALIZACJI);
        }

        @Test
        @DisplayName("WSTRZYMANO → ODRZUCONE")
        void wstrzymanoToOdrzucone() {
            stateMachine.validateTransition(TicketStatus.WSTRZYMANO, TicketStatus.ODRZUCONE);
        }

        @Test
        @DisplayName("ZAKONCZONE → ZAMKNIETE")
        void zakonczoneToZamkniete() {
            stateMachine.validateTransition(
                    TicketStatus.ZAKONCZONE, TicketStatus.ZAMKNIETE);
        }

        @Test
        @DisplayName("ZAKONCZONE → W_REALIZACJI (zwrot do realizacji)")
        void zakonczoneToWRealizacji() {
            stateMachine.validateTransition(
                    TicketStatus.ZAKONCZONE, TicketStatus.W_REALIZACJI);
        }
    }

    // =========================================================
    // validateTransition — niedozwolone przejścia
    // =========================================================

    @Nested
    @DisplayName("validateTransition — niedozwolone przejścia")
    class ForbiddenTransitions {

        @Test
        @DisplayName("NOWE → W_REALIZACJI — rzuca BusinessValidationException")
        void noweToWRealizacji() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.NOWE, TicketStatus.W_REALIZACJI))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("NOWE")
                    .hasMessageContaining("W_REALIZACJI");
        }

        @Test
        @DisplayName("NOWE → ZAMKNIETE — rzuca BusinessValidationException")
        void noweToZamkniete() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.NOWE, TicketStatus.ZAMKNIETE))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("ZAMKNIETE → cokolwiek — terminal, rzuca BusinessValidationException")
        void zamknieteToCzykolwiek() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.ZAMKNIETE, TicketStatus.NOWE))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("ODRZUCONE → cokolwiek — terminal, rzuca BusinessValidationException")
        void odrzuconeToCzykolwiek() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.ODRZUCONE, TicketStatus.NOWE))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("W_REALIZACJI → NOWE — cofnięcie niedozwolone")
        void wRealizacjiToNowe() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.W_REALIZACJI, TicketStatus.NOWE))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("ZAKONCZONE → ODRZUCONE — niedozwolone")
        void zakonczoneToOdrzucone() {
            assertThatThrownBy(
                            () ->
                                    stateMachine.validateTransition(
                                            TicketStatus.ZAKONCZONE,
                                            TicketStatus.ODRZUCONE))
                    .isInstanceOf(BusinessValidationException.class);
        }
    }

    // =========================================================
    // getAllowedNextStatuses
    // =========================================================

    @Nested
    @DisplayName("getAllowedNextStatuses — zwraca zbiór dozwolonych statusów")
    class GetAllowedNextStatuses {

        @Test
        @DisplayName("NOWE — zwraca ZAPLANOWANO i ODRZUCONE")
        void noweReturnsCorrectSet() {
            Set<TicketStatus> allowed = stateMachine.getAllowedNextStatuses(TicketStatus.NOWE);
            assertThat(allowed)
                    .containsExactlyInAnyOrder(TicketStatus.ZAPLANOWANO, TicketStatus.ODRZUCONE);
        }

        @Test
        @DisplayName("ZAMKNIETE — zwraca pusty zbiór")
        void zamknietReturnsEmpty() {
            Set<TicketStatus> allowed = stateMachine.getAllowedNextStatuses(TicketStatus.ZAMKNIETE);
            assertThat(allowed).isEmpty();
        }

        @Test
        @DisplayName("ODRZUCONE — zwraca pusty zbiór")
        void odrzuconeReturnsEmpty() {
            Set<TicketStatus> allowed = stateMachine.getAllowedNextStatuses(TicketStatus.ODRZUCONE);
            assertThat(allowed).isEmpty();
        }

        @Test
        @DisplayName("W_REALIZACJI — zwraca WSTRZYMANO i ZAKONCZONE")
        void wRealizacjiReturnsCorrectSet() {
            Set<TicketStatus> allowed =
                    stateMachine.getAllowedNextStatuses(TicketStatus.W_REALIZACJI);
            assertThat(allowed)
                    .containsExactlyInAnyOrder(
                            TicketStatus.WSTRZYMANO, TicketStatus.ZAKONCZONE);
        }
    }
}

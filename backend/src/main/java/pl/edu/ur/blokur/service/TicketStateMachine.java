package pl.edu.ur.blokur.service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.models.TicketStatus;

/**
 * Silnik state-machine dla statusów zgłoszenia. Definiuje dozwolone przejścia między statusami i
 * waliduje każdą próbę zmiany przed jej wykonaniem.
 *
 * <p>Cykl życia: NOWE → ZAPLANOWANO → W_REALIZACJI → ZAKONCZONE → ZAMKNIETE.
 * Alternatywne ścieżki: WSTRZYMANO (oczekiwanie) lub ODRZUCONE (zakończenie negatywne).
 */
@Component
public class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
                TicketStatus.NOWE, EnumSet.of(TicketStatus.ZAPLANOWANO, TicketStatus.ODRZUCONE));

        ALLOWED_TRANSITIONS.put(
                TicketStatus.ZAPLANOWANO,
                EnumSet.of(
                        TicketStatus.W_REALIZACJI,
                        TicketStatus.WSTRZYMANO,
                        TicketStatus.ODRZUCONE));

        ALLOWED_TRANSITIONS.put(
                TicketStatus.W_REALIZACJI,
                EnumSet.of(TicketStatus.WSTRZYMANO, TicketStatus.ZAKONCZONE));

        ALLOWED_TRANSITIONS.put(
                TicketStatus.WSTRZYMANO,
                EnumSet.of(TicketStatus.W_REALIZACJI, TicketStatus.ODRZUCONE));

        ALLOWED_TRANSITIONS.put(
                TicketStatus.ZAKONCZONE,
                EnumSet.of(TicketStatus.ZAMKNIETE, TicketStatus.W_REALIZACJI));

        ALLOWED_TRANSITIONS.put(TicketStatus.ZAMKNIETE, EnumSet.noneOf(TicketStatus.class));

        ALLOWED_TRANSITIONS.put(TicketStatus.ODRZUCONE, EnumSet.noneOf(TicketStatus.class));
    }

    /**
     * Waliduje przejście statusu. Rzuca wyjątek gdy przejście jest niedozwolone.
     *
     * @param from aktualny status zgłoszenia
     * @param to docelowy status zgłoszenia
     * @throws BusinessValidationException gdy przejście jest niedozwolone
     */
    public void validateTransition(TicketStatus from, TicketStatus to) {
        Set<TicketStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TicketStatus.class));
        if (!allowed.contains(to)) {
            throw new BusinessValidationException(
                    "Niedozwolona zmiana statusu: " + from.name() + " → " + to.name());
        }
    }

    /**
     * Zwraca zbiór statusów, do których można przejść z podanego stanu.
     *
     * @param current aktualny status zgłoszenia
     * @return zbiór dozwolonych statusów docelowych
     */
    public Set<TicketStatus> getAllowedNextStatuses(TicketStatus current) {
        return ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(TicketStatus.class));
    }
}

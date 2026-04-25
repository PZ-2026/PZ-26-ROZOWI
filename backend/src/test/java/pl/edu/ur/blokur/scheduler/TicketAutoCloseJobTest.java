package pl.edu.ur.blokur.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketHistory;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.TicketHistoryRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.service.BusinessHoursCalculator;
import pl.edu.ur.blokur.service.TicketService;

/**
 * Testy jednostkowe dla {@link TicketAutoCloseJob}. Weryfikują logikę automatycznego zamykania
 * zgłoszeń po upływie 5 dni roboczych w stanie ZAKONCZONE_DO_WERYFIKACJI.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketAutoCloseJob — automatyczne zamykanie zgłoszeń")
class TicketAutoCloseJobTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketHistoryRepository ticketHistoryRepository;
    @Mock private BusinessHoursCalculator businessHoursCalculator;
    @Mock private TicketService ticketService;

    @InjectMocks private TicketAutoCloseJob job;

    private UUID ticketId;
    private Ticket ticket;
    private User author;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("Jan");
        author.setLastName("Kowalski");
        author.setEmail("jan@blokur.pl");

        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setTicketNumber("ZGL-2026-0001");
        ticket.setStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI);
        ticket.setTitle("Test usterki");
        ticket.setDescription("Opis usterki");
        ticket.setAuthor(author);
    }

    // -----------------------------------------------------------------------
    // autoCloseExpiredTickets — brak kandydatów
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("autoCloseExpiredTickets — brak kandydatów")
    class NoCandidates {

        @Test
        @DisplayName("Brak zgłoszeń ZAKONCZONE_DO_WERYFIKACJI — serwis nie jest wywoływany")
        void givenNoCandidates_whenJob_thenServiceNotCalled() {
            when(ticketRepository.findAllByTicketStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI))
                    .thenReturn(List.of());

            job.autoCloseExpiredTickets();

            verify(ticketService, never()).autoCloseTicket(any());
        }
    }

    // -----------------------------------------------------------------------
    // processCandidate — SLA nie przekroczone
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("processCandidate — SLA nie przekroczone")
    class SlaNotExceeded {

        @Test
        @DisplayName("39 godzin roboczych (próg 40h) — zgłoszenie NIE jest zamykane")
        void givenElapsed39Hours_whenProcess_thenNotClosed() {
            TicketHistory history = buildHistory(LocalDateTime.now().minusDays(6));
            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            ticketId, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of(history));
            when(businessHoursCalculator.calculate(any(), any())).thenReturn(39.0);
            // hoursPerDay() wywołuje calculate z referencyjnym poniedziałkiem
            when(businessHoursCalculator.calculate(
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(0, 0)),
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(23, 59))))
                    .thenReturn(8.0);

            job.processCandidate(ticket);

            verify(ticketService, never()).autoCloseTicket(any());
        }
    }

    // -----------------------------------------------------------------------
    // processCandidate — SLA przekroczone
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("processCandidate — SLA przekroczone")
    class SlaExceeded {

        @Test
        @DisplayName("40 godzin roboczych (dokładnie próg) — zgłoszenie jest zamykane")
        void givenElapsed40Hours_whenProcess_thenClosed() {
            TicketHistory history = buildHistory(LocalDateTime.now().minusDays(7));
            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            ticketId, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of(history));
            when(businessHoursCalculator.calculate(
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(0, 0)),
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(23, 59))))
                    .thenReturn(8.0);
            when(businessHoursCalculator.calculate(
                            eq(history.getCreatedAt()), any(LocalDateTime.class)))
                    .thenReturn(40.0);

            job.processCandidate(ticket);

            verify(ticketService, times(1)).autoCloseTicket(ticketId);
        }

        @Test
        @DisplayName("55 godzin roboczych (ponad próg) — zgłoszenie jest zamykane")
        void givenElapsed55Hours_whenProcess_thenClosed() {
            TicketHistory history = buildHistory(LocalDateTime.now().minusDays(9));
            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            ticketId, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of(history));
            when(businessHoursCalculator.calculate(
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(0, 0)),
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(23, 59))))
                    .thenReturn(8.0);
            when(businessHoursCalculator.calculate(
                            eq(history.getCreatedAt()), any(LocalDateTime.class)))
                    .thenReturn(55.0);

            job.processCandidate(ticket);

            verify(ticketService, times(1)).autoCloseTicket(ticketId);
        }
    }

    // -----------------------------------------------------------------------
    // processCandidate — brak historii
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("processCandidate — brak wpisu historii")
    class MissingHistory {

        @Test
        @DisplayName("Brak wpisu historii — zgłoszenie NIE jest zamykane")
        void givenNoHistoryEntry_whenProcess_thenNotClosed() {
            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            ticketId, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of());

            job.processCandidate(ticket);

            verify(ticketService, never()).autoCloseTicket(any());
        }
    }

    // -----------------------------------------------------------------------
    // processCandidate — wiele zgłoszeń
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("autoCloseExpiredTickets — wiele zgłoszeń")
    class MultipleTickets {

        @Test
        @DisplayName("Dwa zgłoszenia: jedno przekroczone, jedno nie — tylko jedno zamykane")
        void givenTwoTickets_whenJobRuns_thenOnlyExpiredClosed() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            Ticket ticket1 = buildTicket(id1, "ZGL-2026-0001");
            Ticket ticket2 = buildTicket(id2, "ZGL-2026-0002");

            when(ticketRepository.findAllByTicketStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI))
                    .thenReturn(List.of(ticket1, ticket2));

            TicketHistory history1 = buildHistory(LocalDateTime.now().minusDays(8));
            TicketHistory history2 = buildHistory(LocalDateTime.now().minusDays(3));

            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            id1, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of(history1));
            when(ticketHistoryRepository.findByTicketIdAndStatusOrderByCreatedAtDesc(
                            id2, "ZAKONCZONE_DO_WERYFIKACJI"))
                    .thenReturn(List.of(history2));

            when(businessHoursCalculator.calculate(
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(0, 0)),
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(23, 59))))
                    .thenReturn(8.0);
            when(businessHoursCalculator.calculate(
                            eq(history1.getCreatedAt()), any(LocalDateTime.class)))
                    .thenReturn(48.0);
            when(businessHoursCalculator.calculate(
                            eq(history2.getCreatedAt()), any(LocalDateTime.class)))
                    .thenReturn(20.0);

            job.autoCloseExpiredTickets();

            verify(ticketService, times(1)).autoCloseTicket(id1);
            verify(ticketService, never()).autoCloseTicket(id2);
        }
    }

    // -----------------------------------------------------------------------
    // hoursPerDay
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("hoursPerDay — obliczenie godzin roboczych w dobie")
    class HoursPerDay {

        @Test
        @DisplayName("Konfiguracja 8–16: hoursPerDay zwraca 8")
        void givenDefault8to16_whenHoursPerDay_thenReturns8() {
            when(businessHoursCalculator.calculate(
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(0, 0)),
                            eq(java.time.LocalDate.of(2024, 1, 1).atTime(23, 59))))
                    .thenReturn(8.0);

            int hours = job.hoursPerDay();

            assertThat(hours).isEqualTo(8);
        }
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private TicketHistory buildHistory(LocalDateTime createdAt) {
        TicketHistory h = new TicketHistory();
        h.setId(UUID.randomUUID());
        h.setStatus("ZAKONCZONE_DO_WERYFIKACJI");
        h.setChangedBy(author);
        h.setCreatedAt(createdAt);
        return h;
    }

    private Ticket buildTicket(UUID id, String number) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setTicketNumber(number);
        t.setStatus(TicketStatus.ZAKONCZONE_DO_WERYFIKACJI);
        t.setTitle("Usterka");
        t.setDescription("Opis");
        t.setAuthor(author);
        return t;
    }
}

package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.TicketCommentDto;
import pl.edu.ur.blokur.dto.TicketCommentRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketComment;
import pl.edu.ur.blokur.models.TicketCommentType;
import pl.edu.ur.blokur.models.TicketStatus;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.TicketCommentRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketCommentRepository ticketCommentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TicketCommentService ticketCommentService;

    private UUID ticketId;
    private Ticket ticket;
    private User mieszkaniec;
    private User konserwator;
    private User zarzadca;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        ticket = new Ticket();
        ticket.setId(ticketId);

        mieszkaniec = new User();
        mieszkaniec.setId(UUID.randomUUID());
        mieszkaniec.setEmail("mieszkaniec@test.pl");
        mieszkaniec.setRole("MIESZKANIEC");
        mieszkaniec.setFirstName("Jan");
        mieszkaniec.setLastName("Kowalski");

        konserwator = new User();
        konserwator.setId(UUID.randomUUID());
        konserwator.setEmail("konserwator@test.pl");
        konserwator.setRole("KONSERWATOR");
        konserwator.setFirstName("Adam");
        konserwator.setLastName("Nowak");

        zarzadca = new User();
        zarzadca.setId(UUID.randomUUID());
        zarzadca.setEmail("zarzadca@test.pl");
        zarzadca.setRole("ZARZADCA");
        zarzadca.setFirstName("Anna");
        zarzadca.setLastName("Zarzadca");
    }

    @Nested
    @DisplayName("Dodawanie komentarzy")
    class AddCommentTests {

        @Test
        @DisplayName("Zarzadca moze dodac komentarz wewnetrzny i publiczny")
        void zarzadcaCanAddAnyComment() {
            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Test komentarza");
            request.setCommentType(TicketCommentType.WEWNETRZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(zarzadca.getEmail())).thenReturn(Optional.of(zarzadca));
            when(ticketCommentRepository.save(any(TicketComment.class)))
                    .thenAnswer(
                            inv -> {
                                TicketComment tc = inv.getArgument(0);
                                tc.setId(UUID.randomUUID());
                                return tc;
                            });

            TicketCommentDto result =
                    ticketCommentService.addComment(ticketId, request, zarzadca.getEmail());

            assertThat(result).isNotNull();
            assertThat(result.getCommentType()).isEqualTo(TicketCommentType.WEWNETRZNY);
        }

        @Test
        @DisplayName("Konserwator moze dodac tylko komentarz wewnetrzny do przypisanego zgloszenia")
        void konserwatorCanAddInternalCommentToAssignedTicket() {
            ticket.setAssignedTo(konserwator);

            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Praca w toku");
            request.setCommentType(TicketCommentType.WEWNETRZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(konserwator.getEmail()))
                    .thenReturn(Optional.of(konserwator));
            when(ticketCommentRepository.save(any(TicketComment.class)))
                    .thenAnswer(
                            inv -> {
                                TicketComment tc = inv.getArgument(0);
                                tc.setId(UUID.randomUUID());
                                return tc;
                            });

            TicketCommentDto result =
                    ticketCommentService.addComment(ticketId, request, konserwator.getEmail());

            assertThat(result).isNotNull();
            assertThat(result.getCommentType()).isEqualTo(TicketCommentType.WEWNETRZNY);
        }

        @Test
        @DisplayName("Konserwator rzuca wyjatek gdy probuje dodac publiczny")
        void konserwatorCannotAddPublicComment() {
            ticket.setAssignedTo(konserwator);

            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Widoczne dla wszystkich");
            request.setCommentType(TicketCommentType.PUBLICZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(konserwator.getEmail()))
                    .thenReturn(Optional.of(konserwator));

            assertThatThrownBy(
                            () ->
                                    ticketCommentService.addComment(
                                            ticketId, request, konserwator.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Konserwator może dodawać tylko wewnętrzne komentarze");
        }

        @Test
        @DisplayName("Konserwator rzuca wyjatek gdy dodaje do nieprzypisanego")
        void konserwatorCannotAddCommentToUnassigned() {
            // ticket nie ma assignedTo

            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Praca w toku");
            request.setCommentType(TicketCommentType.WEWNETRZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(konserwator.getEmail()))
                    .thenReturn(Optional.of(konserwator));

            assertThatThrownBy(
                            () ->
                                    ticketCommentService.addComment(
                                            ticketId, request, konserwator.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("tylko do własnych zgłoszeń");
        }

        @Test
        @DisplayName("Mieszkaniec moze dodac komentarz publiczny")
        void mieszkaniecCanAddPublicComment() {
            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Dodatkowe informacje");
            request.setCommentType(TicketCommentType.PUBLICZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(mieszkaniec.getEmail()))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCommentRepository.save(any(TicketComment.class)))
                    .thenAnswer(
                            inv -> {
                                TicketComment tc = inv.getArgument(0);
                                tc.setId(UUID.randomUUID());
                                return tc;
                            });

            TicketCommentDto result =
                    ticketCommentService.addComment(ticketId, request, mieszkaniec.getEmail());

            assertThat(result).isNotNull();
            assertThat(result.getCommentType()).isEqualTo(TicketCommentType.PUBLICZNY);
        }

        @Test
        @DisplayName("Mieszkaniec nie moze dodac komentarza wewnetrznego")
        void mieszkaniecCannotAddInternalComment() {
            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Ukryte");
            request.setCommentType(TicketCommentType.WEWNETRZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(mieszkaniec.getEmail()))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(
                            () ->
                                    ticketCommentService.addComment(
                                            ticketId, request, mieszkaniec.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("tylko publiczne komentarze");
        }

        @Test
        @DisplayName("Zablokuj dodanie komentarza gdy zgloszenie jest zamkniete")
        void shouldThrowExceptionWhenAddingCommentToClosedTicket() {
            ticket.setStatus(TicketStatus.ZAMKNIETE);
            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Komentarz do zamknietego");
            request.setCommentType(TicketCommentType.PUBLICZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(mieszkaniec.getEmail()))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(
                            () ->
                                    ticketCommentService.addComment(
                                            ticketId, request, mieszkaniec.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Nie można dodawać komentarzy do zamkniętego lub odrzuconego zgłoszenia");
        }

        @Test
        @DisplayName("Zablokuj dodanie komentarza gdy zgloszenie jest odrzucone")
        void shouldThrowExceptionWhenAddingCommentToRejectedTicket() {
            ticket.setStatus(TicketStatus.ODRZUCONE);
            TicketCommentRequest request = new TicketCommentRequest();
            request.setContent("Komentarz do odrzuconego");
            request.setCommentType(TicketCommentType.PUBLICZNY);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(mieszkaniec.getEmail()))
                    .thenReturn(Optional.of(mieszkaniec));

            assertThatThrownBy(
                            () ->
                                    ticketCommentService.addComment(
                                            ticketId, request, mieszkaniec.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Nie można dodawać komentarzy do zamkniętego lub odrzuconego zgłoszenia");
        }
    }

    @Nested
    @DisplayName("Pobieranie komentarzy")
    class GetCommentsTests {

        @Test
        @DisplayName("Mieszkaniec widzi tylko publiczne")
        void mieszkaniecSeesOnlyPublic() {
            TicketCommentDto publicComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Anna Zarzadca",
                            "Publiczny",
                            TicketCommentType.PUBLICZNY,
                            LocalDateTime.now());
            TicketCommentDto internalComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Adam Nowak",
                            "Wewnętrzny",
                            TicketCommentType.WEWNETRZNY,
                            LocalDateTime.now());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(mieszkaniec.getEmail()))
                    .thenReturn(Optional.of(mieszkaniec));
            when(ticketCommentRepository.findCommentsByTicketId(ticketId))
                    .thenReturn(List.of(publicComment, internalComment));

            List<TicketCommentDto> result =
                    ticketCommentService.getComments(ticketId, mieszkaniec.getEmail());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCommentType()).isEqualTo(TicketCommentType.PUBLICZNY);
        }

        @Test
        @DisplayName("Konserwator widzi wszystkie")
        void konserwatorSeesAll() {
            TicketCommentDto publicComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Anna Zarzadca",
                            "Publiczny",
                            TicketCommentType.PUBLICZNY,
                            LocalDateTime.now());
            TicketCommentDto internalComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Adam Nowak",
                            "Wewnętrzny",
                            TicketCommentType.WEWNETRZNY,
                            LocalDateTime.now());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(konserwator.getEmail()))
                    .thenReturn(Optional.of(konserwator));
            when(ticketCommentRepository.findCommentsByTicketId(ticketId))
                    .thenReturn(List.of(publicComment, internalComment));

            List<TicketCommentDto> result =
                    ticketCommentService.getComments(ticketId, konserwator.getEmail());

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Zarzadca widzi wszystkie")
        void zarzadcaSeesAll() {
            TicketCommentDto publicComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Anna Zarzadca",
                            "Publiczny",
                            TicketCommentType.PUBLICZNY,
                            LocalDateTime.now());
            TicketCommentDto internalComment =
                    new TicketCommentDto(
                            UUID.randomUUID(),
                            ticketId,
                            "Adam Nowak",
                            "Wewnętrzny",
                            TicketCommentType.WEWNETRZNY,
                            LocalDateTime.now());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(zarzadca.getEmail())).thenReturn(Optional.of(zarzadca));
            when(ticketCommentRepository.findCommentsByTicketId(ticketId))
                    .thenReturn(List.of(publicComment, internalComment));

            List<TicketCommentDto> result =
                    ticketCommentService.getComments(ticketId, zarzadca.getEmail());

            assertThat(result).hasSize(2);
        }
    }
}

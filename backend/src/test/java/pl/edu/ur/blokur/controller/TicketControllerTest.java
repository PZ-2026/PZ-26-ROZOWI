package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.ur.blokur.dto.TicketDetailDto;
import pl.edu.ur.blokur.dto.TicketFilterParams;
import pl.edu.ur.blokur.dto.TicketRequest;
import pl.edu.ur.blokur.dto.TicketSummaryDto;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.TicketService;

/**
 * Testy jednostkowe kontrolera {@link TicketController}. Weryfikują obsługę żądań HTTP, kody
 * odpowiedzi i walidację danych wejściowych na poziomie warstwy webowej.
 */
@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "test@blokur.pl", roles = "MIESZKANIEC")
@DisplayName("TicketController — warstwa REST")
class TicketControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private TicketService ticketService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private pl.edu.ur.blokur.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID ticketId;
    private UUID categoryId;
    private TicketRequest validRequest;
    private TicketDetailDto sampleDetail;
    private TicketSummaryDto sampleSummary;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        ticketId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        validRequest =
                new TicketRequest("Cieknie kran", "Kran w kuchni cieknie od tygodnia", categoryId);

        sampleDetail = new TicketDetailDto();
        sampleDetail.setId(ticketId);
        sampleDetail.setTicketNumber("ZGL-2026-0001");
        sampleDetail.setTitle("Cieknie kran");
        sampleDetail.setDescription("Kran w kuchni cieknie od tygodnia");
        sampleDetail.setStatus("NOWE");
        sampleDetail.setCategoryName("Awaria hydrauliczna");
        sampleDetail.setAuthorName("Jan Kowalski");
        sampleDetail.setLocationLabel("10");
        sampleDetail.setCreatedAt(LocalDateTime.of(2026, 4, 24, 10, 0));

        sampleSummary =
                new TicketSummaryDto(
                        ticketId,
                        "ZGL-2026-0001",
                        "Cieknie kran",
                        "NOWE",
                        "Awaria hydrauliczna",
                        "Jan Kowalski",
                        null,
                        "10",
                        LocalDateTime.of(2026, 4, 24, 10, 0),
                        null);

        try {
            org.mockito.stubbing.Answer<Void> doFilterAnswer =
                    invocation -> {
                        jakarta.servlet.FilterChain filterChain = invocation.getArgument(2);
                        jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
                        jakarta.servlet.http.HttpServletResponse response =
                                invocation.getArgument(1);
                        filterChain.doFilter(request, response);
                        return null;
                    };
            org.mockito.Mockito.doAnswer(doFilterAnswer)
                    .when(jwtAuthenticationFilter)
                    .doFilter(any(), any(), any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =======================================================
    // POST /api/tickets
    // =======================================================

    @Nested
    @DisplayName("POST /api/tickets")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 201 Created z treścią")
        void shouldReturn201ForValidRequest() throws Exception {
            when(ticketService.create(any(TicketRequest.class), any())).thenReturn(sampleDetail);

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ticketId.toString()))
                    .andExpect(jsonPath("$.ticketNumber").value("ZGL-2026-0001"))
                    .andExpect(jsonPath("$.status").value("NOWE"));
        }

        @Test
        @DisplayName("Pusty tytuł — zwraca 400 Bad Request")
        void shouldReturn400WhenTitleBlank() throws Exception {
            TicketRequest invalid = new TicketRequest("", "Opis", categoryId);

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Pusty opis — zwraca 400 Bad Request")
        void shouldReturn400WhenDescriptionBlank() throws Exception {
            TicketRequest invalid = new TicketRequest("Tytuł", "", categoryId);

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak kategorii — zwraca 400 Bad Request")
        void shouldReturn400WhenCategoryIdMissing() throws Exception {
            TicketRequest invalid = new TicketRequest("Tytuł", "Opis", null);

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Tytuł za długi (>100 znaków) — zwraca 400 Bad Request")
        void shouldReturn400WhenTitleTooLong() throws Exception {
            String longTitle = "A".repeat(101);
            TicketRequest invalid = new TicketRequest(longTitle, "Opis", categoryId);

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Nieistniejąca kategoria — zwraca 404 Not Found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(ticketService.create(any(TicketRequest.class), any()))
                    .thenThrow(
                            new NotFoundException(
                                    "Kategoria o ID " + categoryId + " nie istnieje"));

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Mieszkaniec bez lokalu — zwraca 422 Unprocessable Entity")
        void shouldReturn422WhenResidentHasNoApartment() throws Exception {
            when(ticketService.create(any(TicketRequest.class), any()))
                    .thenThrow(
                            new BusinessValidationException(
                                    "Mieszkaniec nie ma przypisanego lokalu"));

            mockMvc.perform(
                            post("/api/tickets")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =======================================================
    // GET /api/tickets
    // =======================================================

    @Nested
    @DisplayName("GET /api/tickets")
    class GetAllTests {

        @Test
        @DisplayName("Zwraca 200 z listą zgłoszeń")
        void shouldReturn200WithList() throws Exception {
            when(ticketService.getAll(any(), any(TicketFilterParams.class)))
                    .thenReturn(List.of(sampleSummary));

            mockMvc.perform(get("/api/tickets").with(user("test@blokur.pl")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].ticketNumber").value("ZGL-2026-0001"))
                    .andExpect(jsonPath("$[0].status").value("NOWE"));
        }

        @Test
        @DisplayName("Brak zgłoszeń — zwraca 200 z pustą listą")
        void shouldReturn200WithEmptyList() throws Exception {
            when(ticketService.getAll(any(), any(TicketFilterParams.class))).thenReturn(List.of());

            mockMvc.perform(get("/api/tickets").with(user("test@blokur.pl")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Filtrowanie po statusie — przekazuje parametr do serwisu")
        void shouldPassStatusFilterToService() throws Exception {
            when(ticketService.getAll(any(), any(TicketFilterParams.class)))
                    .thenReturn(List.of(sampleSummary));

            mockMvc.perform(
                            get("/api/tickets")
                                    .with(user("test@blokur.pl"))
                                    .param("status", "NOWE"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Filtrowanie po categoryId — zwraca 200")
        void shouldPassCategoryFilterToService() throws Exception {
            when(ticketService.getAll(any(), any(TicketFilterParams.class))).thenReturn(List.of());

            mockMvc.perform(
                            get("/api/tickets")
                                    .with(user("test@blokur.pl"))
                                    .param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk());
        }
    }

    // =======================================================
    // GET /api/tickets/{id}
    // =======================================================

    @Nested
    @DisplayName("GET /api/tickets/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Istniejące zgłoszenie — zwraca 200 ze szczegółami")
        void shouldReturn200WithTicketDetails() throws Exception {
            when(ticketService.getById(eq(ticketId), any())).thenReturn(sampleDetail);

            mockMvc.perform(get("/api/tickets/{id}", ticketId).with(user("test@blokur.pl")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ticketId.toString()))
                    .andExpect(jsonPath("$.ticketNumber").value("ZGL-2026-0001"))
                    .andExpect(jsonPath("$.title").value("Cieknie kran"))
                    .andExpect(jsonPath("$.status").value("NOWE"));
        }

        @Test
        @DisplayName("Nieistniejące zgłoszenie — zwraca 404 Not Found")
        void shouldReturn404WhenTicketNotFound() throws Exception {
            when(ticketService.getById(eq(ticketId), any()))
                    .thenThrow(
                            new NotFoundException("Zgłoszenie o ID " + ticketId + " nie istnieje"));

            mockMvc.perform(get("/api/tickets/{id}", ticketId).with(user("test@blokur.pl")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Brak uprawnień do zgłoszenia — zwraca 422 Unprocessable Entity")
        void shouldReturn422WhenAccessDenied() throws Exception {
            when(ticketService.getById(eq(ticketId), any()))
                    .thenThrow(new BusinessValidationException("Brak dostępu do zgłoszenia"));

            mockMvc.perform(get("/api/tickets/{id}", ticketId).with(user("test@blokur.pl")))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Mieszkaniec nie widzi notatki wewnętrznej — internalNote jest null")
        void shouldNotExposeInternalNoteToResident() throws Exception {
            sampleDetail.setInternalNote(null);

            when(ticketService.getById(eq(ticketId), any())).thenReturn(sampleDetail);

            mockMvc.perform(get("/api/tickets/{id}", ticketId).with(user("test@blokur.pl")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.internalNote").doesNotExist());
        }

        @Test
        @DisplayName("Zarządca widzi notatkę wewnętrzną — internalNote jest zwracana")
        void shouldExposeInternalNoteToZarzadca() throws Exception {
            sampleDetail.setInternalNote("Notatka wewnętrzna dla zarządcy");

            when(ticketService.getById(eq(ticketId), any())).thenReturn(sampleDetail);

            mockMvc.perform(get("/api/tickets/{id}", ticketId).with(user("test@blokur.pl")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.internalNote").value("Notatka wewnętrzna dla zarządcy"));
        }
    }
}

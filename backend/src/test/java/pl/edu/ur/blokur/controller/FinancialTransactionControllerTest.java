package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.edu.ur.blokur.dto.ApartmentTransactionsResponse;
import pl.edu.ur.blokur.dto.CsvImportErrorDto;
import pl.edu.ur.blokur.dto.CsvImportResultDto;
import pl.edu.ur.blokur.dto.FinancialTransactionRequest;
import pl.edu.ur.blokur.dto.FinancialTransactionResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.FinancialTransactionService;
import pl.edu.ur.blokur.service.LoginAttemptService;

/**
 * Testy jednostkowe kontrolera {@link FinancialTransactionController}. Weryfikują obsługę żądań
 * HTTP, autoryzację ról oraz walidację danych wejściowych na poziomie warstwy webowej.
 */
@WebMvcTest(FinancialTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FinancialTransactionController — warstwa REST")
class FinancialTransactionControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private FinancialTransactionService financialTransactionService;

    @MockitoBean private JwtService jwtService;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @MockitoBean private LoginAttemptService loginAttemptService;

    @MockitoBean private pl.edu.ur.blokur.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID apartmentId;
    private FinancialTransactionRequest validRequest;
    private FinancialTransactionResponse transactionResponse;
    private ApartmentTransactionsResponse apartmentTransactionsResponse;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        validRequest =
                new FinancialTransactionRequest(
                        "WPLATA",
                        new BigDecimal("500.00"),
                        "Wpłata czynszu za kwiecień 2026",
                        LocalDate.of(2026, 4, 15));

        transactionResponse =
                new FinancialTransactionResponse(
                        UUID.randomUUID(),
                        apartmentId,
                        "WPLATA",
                        new BigDecimal("500.00"),
                        "Wpłata czynszu za kwiecień 2026",
                        LocalDate.of(2026, 4, 15),
                        "mock.zarzadca1@blokur.pl");

        apartmentTransactionsResponse =
                new ApartmentTransactionsResponse(
                        new BigDecimal("700.00"), List.of(transactionResponse));

        // Zapobiega blokowaniu żądań przez zamockowany filtr
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
    // GET /api/apartments/{id}/transactions
    // =======================================================

    @Nested
    @DisplayName("GET /api/apartments/{id}/transactions")
    class GetTransactionsTests {

        @Test
        @DisplayName("Zwraca 200 z saldem i listą transakcji")
        void shouldReturn200() throws Exception {
            when(financialTransactionService.getTransactionsForApartment(apartmentId))
                    .thenReturn(apartmentTransactionsResponse);

            mockMvc.perform(get("/api/apartments/{id}/transactions", apartmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentBalance").value(700.00))
                    .andExpect(jsonPath("$.transactions").isArray())
                    .andExpect(jsonPath("$.transactions[0].type").value("WPLATA"));
        }

        @Test
        @DisplayName("Nieistniejący lokal — zwraca 404")
        void shouldReturn404WhenApartmentNotFound() throws Exception {
            when(financialTransactionService.getTransactionsForApartment(apartmentId))
                    .thenThrow(
                            new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje"));

            mockMvc.perform(get("/api/apartments/{id}/transactions", apartmentId))
                    .andExpect(status().isNotFound());
        }
    }

    // =======================================================
    // POST /api/apartments/{id}/transactions
    // =======================================================

    @Nested
    @DisplayName("POST /api/apartments/{id}/transactions")
    class CreateTransactionTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 201 Created")
        void shouldReturn201ForValidRequest() throws Exception {
            java.security.Principal principal = () -> "mock.zarzadca1@blokur.pl";

            when(financialTransactionService.createTransaction(
                            eq(apartmentId),
                            any(FinancialTransactionRequest.class),
                            eq("mock.zarzadca1@blokur.pl")))
                    .thenReturn(transactionResponse);

            mockMvc.perform(
                            post("/api/apartments/{id}/transactions", apartmentId)
                                    .principal(principal)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("WPLATA"))
                    .andExpect(jsonPath("$.amount").value(500.00));
        }

        @Test
        @DisplayName("Brak pola type — zwraca 400 Bad Request")
        void shouldReturn400WhenTypeMissing() throws Exception {
            FinancialTransactionRequest invalid =
                    new FinancialTransactionRequest(
                            null, new BigDecimal("500.00"), "Opis", LocalDate.of(2026, 4, 15));

            mockMvc.perform(
                            post("/api/apartments/{id}/transactions", apartmentId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak pola amount — zwraca 400 Bad Request")
        void shouldReturn400WhenAmountMissing() throws Exception {
            FinancialTransactionRequest invalid =
                    new FinancialTransactionRequest(
                            "WPLATA", null, "Opis", LocalDate.of(2026, 4, 15));

            mockMvc.perform(
                            post("/api/apartments/{id}/transactions", apartmentId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak pola transactionDate — zwraca 400 Bad Request")
        void shouldReturn400WhenTransactionDateMissing() throws Exception {
            FinancialTransactionRequest invalid =
                    new FinancialTransactionRequest(
                            "WPLATA", new BigDecimal("500.00"), "Opis", null);

            mockMvc.perform(
                            post("/api/apartments/{id}/transactions", apartmentId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =======================================================
    // POST /api/finance/import
    // =======================================================

    @Nested
    @DisplayName("POST /api/finance/import")
    class ImportTransactionsTests {

        @Test
        @DisplayName("Poprawny plik - zwraca 200 OK z podsumowaniem")
        void shouldReturn200ForValidFile() throws Exception {
            java.security.Principal principal = () -> "mock.zarzadca1@blokur.pl";

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "test.csv",
                            "text/csv",
                            "apartment_id,date,type,amount,description\n123,2026-04-15,WPLATA,150.00,Czynsz"
                                    .getBytes());

            CsvImportResultDto resultDto = new CsvImportResultDto(1, 0, List.of());

            when(financialTransactionService.importTransactionsFromCsv(
                            any(), eq("mock.zarzadca1@blokur.pl")))
                    .thenReturn(resultDto);

            mockMvc.perform(
                            MockMvcRequestBuilders.multipart("/api/finance/import")
                                    .file(file)
                                    .principal(principal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.importedCount").value(1))
                    .andExpect(jsonPath("$.errorCount").value(0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Częściowo błędny plik - zwraca 200 OK z błędami")
        void shouldReturn200WithErrors() throws Exception {
            java.security.Principal principal = () -> "mock.zarzadca1@blokur.pl";

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "test.csv",
                            "text/csv",
                            "apartment_id,date,type,amount,description\ninvalid,date,typ,amount,opis"
                                    .getBytes());

            CsvImportResultDto resultDto =
                    new CsvImportResultDto(0, 1, List.of(new CsvImportErrorDto(2, "Błąd UUID")));

            when(financialTransactionService.importTransactionsFromCsv(
                            any(), eq("mock.zarzadca1@blokur.pl")))
                    .thenReturn(resultDto);

            mockMvc.perform(
                            MockMvcRequestBuilders.multipart("/api/finance/import")
                                    .file(file)
                                    .principal(principal))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.importedCount").value(0))
                    .andExpect(jsonPath("$.errorCount").value(1))
                    .andExpect(jsonPath("$.errors[0].line").value(2))
                    .andExpect(jsonPath("$.errors[0].message").value("Błąd UUID"));
        }
    }
}

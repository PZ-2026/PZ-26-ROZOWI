package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.ur.blokur.dto.InspectionRequest;
import pl.edu.ur.blokur.dto.InspectionResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.ScopeType;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.InspectionService;
import pl.edu.ur.blokur.service.LoginAttemptService;

/**
 * Testy jednostkowe kontrolera {@link InspectionController}. Weryfikują obsługę żądań HTTP, kody
 * odpowiedzi oraz walidację danych wejściowych na poziomie warstwy webowej.
 */
@WebMvcTest(InspectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InspectionController — warstwa REST")
class InspectionControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private InspectionService inspectionService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private pl.edu.ur.blokur.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID inspectionId;
    private UUID scopeId;
    private InspectionRequest validRequest;
    private InspectionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        inspectionId = UUID.randomUUID();
        scopeId = UUID.randomUUID();

        validRequest =
                new InspectionRequest(
                        "Przegląd gazowy",
                        "Coroczny przegląd instalacji gazowej",
                        LocalDateTime.of(2026, 8, 15, 10, 0),
                        ScopeType.BUDYNEK,
                        scopeId);

        sampleResponse =
                new InspectionResponse(
                        inspectionId,
                        "Przegląd gazowy",
                        "Coroczny przegląd instalacji gazowej",
                        LocalDateTime.of(2026, 8, 15, 10, 0),
                        ScopeType.BUDYNEK,
                        scopeId,
                        "Adam Zarządca",
                        LocalDateTime.of(2026, 4, 24, 8, 0));

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
    // POST /api/inspections
    // =======================================================

    @Nested
    @DisplayName("POST /api/inspections")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 201 Created z treścią")
        void shouldReturn201ForValidRequest() throws Exception {
            when(inspectionService.create(any(InspectionRequest.class), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(
                            post("/api/inspections")
                                    .principal(() -> "testUser")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(inspectionId.toString()))
                    .andExpect(jsonPath("$.title").value("Przegląd gazowy"))
                    .andExpect(jsonPath("$.scopeType").value("BUDYNEK"));
        }

        @Test
        @DisplayName("Pusty tytuł — zwraca 400 Bad Request")
        void shouldReturn400WhenTitleBlank() throws Exception {
            InspectionRequest invalid =
                    new InspectionRequest(
                            "",
                            "Opis",
                            LocalDateTime.of(2026, 8, 15, 10, 0),
                            ScopeType.BUDYNEK,
                            scopeId);

            mockMvc.perform(
                            post("/api/inspections")
                                    .principal(() -> "testUser")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak daty — zwraca 400 Bad Request")
        void shouldReturn400WhenScheduledAtMissing() throws Exception {
            InspectionRequest invalid =
                    new InspectionRequest("Przegląd", "Opis", null, ScopeType.BUDYNEK, scopeId);

            mockMvc.perform(
                            post("/api/inspections")
                                    .principal(() -> "testUser")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak scopeId — zwraca 400 Bad Request")
        void shouldReturn400WhenScopeIdMissing() throws Exception {
            InspectionRequest invalid =
                    new InspectionRequest(
                            "Przegląd",
                            "Opis",
                            LocalDateTime.of(2026, 8, 15, 10, 0),
                            ScopeType.BUDYNEK,
                            null);

            mockMvc.perform(
                            post("/api/inspections")
                                    .principal(() -> "testUser")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Nieistniejący zasięg — zwraca 404 Not Found")
        void shouldReturn404WhenScopeNotFound() throws Exception {
            when(inspectionService.create(any(InspectionRequest.class), any()))
                    .thenThrow(new NotFoundException("Budynek o ID " + scopeId + " nie istnieje"));

            mockMvc.perform(
                            post("/api/inspections")
                                    .principal(() -> "testUser")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // =======================================================
    // GET /api/inspections
    // =======================================================

    @Nested
    @DisplayName("GET /api/inspections")
    class GetAllTests {

        @Test
        @DisplayName("Zwraca 200 z listą przeglądów")
        void shouldReturn200WithList() throws Exception {
            when(inspectionService.getAll(any())).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/inspections").principal(() -> "testUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("Przegląd gazowy"))
                    .andExpect(jsonPath("$[0].scopeType").value("BUDYNEK"));
        }

        @Test
        @DisplayName("Brak przeglądów — zwraca 200 z pustą listą")
        void shouldReturn200WithEmptyList() throws Exception {
            when(inspectionService.getAll(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/inspections").principal(() -> "testUser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =======================================================
    // PUT /api/inspections/{id}
    // =======================================================

    @Nested
    @DisplayName("PUT /api/inspections/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 200 z zaktualizowanym przeglądem")
        void shouldReturn200ForValidUpdate() throws Exception {
            when(inspectionService.update(eq(inspectionId), any(InspectionRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(
                            put("/api/inspections/{id}", inspectionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(inspectionId.toString()))
                    .andExpect(jsonPath("$.title").value("Przegląd gazowy"));
        }

        @Test
        @DisplayName("Nieistniejący przegląd — zwraca 404 Not Found")
        void shouldReturn404WhenInspectionNotFound() throws Exception {
            when(inspectionService.update(eq(inspectionId), any(InspectionRequest.class)))
                    .thenThrow(
                            new NotFoundException(
                                    "Przegląd o ID " + inspectionId + " nie istnieje"));

            mockMvc.perform(
                            put("/api/inspections/{id}", inspectionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Pusty tytuł w aktualizacji — zwraca 400 Bad Request")
        void shouldReturn400WhenTitleBlankOnUpdate() throws Exception {
            InspectionRequest invalid =
                    new InspectionRequest(
                            " ",
                            "Opis",
                            LocalDateTime.of(2026, 8, 15, 10, 0),
                            ScopeType.BUDYNEK,
                            scopeId);

            mockMvc.perform(
                            put("/api/inspections/{id}", inspectionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    // =======================================================
    // DELETE /api/inspections/{id}
    // =======================================================

    @Nested
    @DisplayName("DELETE /api/inspections/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Istniejący przegląd — zwraca 204 No Content")
        void shouldReturn204ForExistingInspection() throws Exception {
            mockMvc.perform(delete("/api/inspections/{id}", inspectionId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Nieistniejący przegląd — zwraca 404 Not Found")
        void shouldReturn404WhenInspectionNotFound() throws Exception {
            doThrow(new NotFoundException("Przegląd o ID " + inspectionId + " nie istnieje"))
                    .when(inspectionService)
                    .delete(inspectionId);

            mockMvc.perform(delete("/api/inspections/{id}", inspectionId))
                    .andExpect(status().isNotFound());
        }
    }
}

package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.edu.ur.blokur.dto.CategoryRequest;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.dto.SlaRequest;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtAuthenticationFilter;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.TicketCategoryService;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminCategoryController — zarządzanie kategoriami zgłoszeń")
class AdminCategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private TicketCategoryService categoryService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        categoryId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/admin/categories — tworzenie kategorii")
    class CreateCategory {

        @Test
        @DisplayName("Poprawne dane — zwraca 201 z nową kategorią")
        void shouldReturn201WithCreatedCategory() throws Exception {
            CategoryRequest req = new CategoryRequest();
            req.setName("Awaria");

            CategoryResponse resp = new CategoryResponse(categoryId, "Awaria");
            when(categoryService.createCategory(any())).thenReturn(resp);

            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Awaria"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/categories/{id} — aktualizacja kategorii")
    class UpdateCategory {

        @Test
        @DisplayName("Poprawne dane — zwraca 200 z zaktualizowaną kategorią")
        void shouldReturn200WithUpdatedCategory() throws Exception {
            CategoryRequest req = new CategoryRequest();
            req.setName("Usterka");

            CategoryResponse resp = new CategoryResponse(categoryId, "Usterka");
            when(categoryService.updateCategory(eq(categoryId), any())).thenReturn(resp);

            mockMvc.perform(put("/api/admin/categories/" + categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Usterka"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/categories/{id}/sla — ustawienie SLA")
    class SetSla {

        @Test
        @DisplayName("Poprawne dane — zwraca 204")
        void shouldReturn204WhenSlaSet() throws Exception {
            SlaRequest req = new SlaRequest();
            req.setSlaHours(8);

            doNothing().when(categoryService).setSlaHours(eq(categoryId), any());

            mockMvc.perform(patch("/api/admin/categories/" + categoryId + "/sla")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/categories/{id}/deactivate — deaktywacja kategorii")
    class DeactivateCategory {

        @Test
        @DisplayName("Istniejąca kategoria — zwraca 204")
        void shouldReturn204WhenDeactivated() throws Exception {
            doNothing().when(categoryService).deactivateCategory(categoryId);

            mockMvc.perform(patch("/api/admin/categories/" + categoryId + "/deactivate"))
                    .andExpect(status().isNoContent());
        }
    }
}

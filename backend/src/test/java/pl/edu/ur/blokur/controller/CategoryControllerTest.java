package pl.edu.ur.blokur.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.ur.blokur.dto.CategoryResponse;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtAuthenticationFilter;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.TicketCategoryService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CategoryController — lista aktywnych kategorii")
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TicketCategoryService categoryService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/categories — zwraca 200 z listą kategorii")
    void shouldReturn200WithCategoryList() throws Exception {
        CategoryResponse cat = new CategoryResponse(UUID.randomUUID(), "Awaria", 8);
        when(categoryService.getActiveCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Awaria"));
    }

    @Test
    @DisplayName("GET /api/categories — zwraca 200 z pustą listą gdy brak kategorii")
    void shouldReturn200WithEmptyList() throws Exception {
        when(categoryService.getActiveCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}

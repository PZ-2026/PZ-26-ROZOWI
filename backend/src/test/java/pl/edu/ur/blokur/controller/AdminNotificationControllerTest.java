package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
import pl.edu.ur.blokur.dto.NotificationConfigResponse;
import pl.edu.ur.blokur.dto.UpdateNotificationConfigRequest;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtAuthenticationFilter;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.NotificationConfigService;

@WebMvcTest(AdminNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminNotificationController — konfiguracja powiadomień PUSH")
class AdminNotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private NotificationConfigService notificationConfigService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("GET /api/admin/notifications/settings — lista konfiguracji")
    class GetAll {

        @Test
        @DisplayName("Zwraca 200 z listą konfiguracji powiadomień")
        void shouldReturn200WithConfigList() throws Exception {
            NotificationConfigResponse resp =
                    new NotificationConfigResponse("OGLOSZENIE", true, "Ogłoszenie");
            when(notificationConfigService.getAll()).thenReturn(List.of(resp));

            mockMvc.perform(get("/api/admin/notifications/settings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].eventType").value("OGLOSZENIE"))
                    .andExpect(jsonPath("$[0].enabled").value(true));
        }

        @Test
        @DisplayName("Zwraca 200 z pustą listą gdy brak konfiguracji")
        void shouldReturn200WithEmptyList() throws Exception {
            when(notificationConfigService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/notifications/settings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/notifications/settings/{eventType} — aktualizacja flagi")
    class Update {

        @Test
        @DisplayName("Poprawne żądanie — zwraca 200 z zaktualizowaną konfiguracją")
        void shouldReturn200WithUpdatedConfig() throws Exception {
            UpdateNotificationConfigRequest req = new UpdateNotificationConfigRequest();
            req.setEnabled(false);

            NotificationConfigResponse updated =
                    new NotificationConfigResponse("OGLOSZENIE", false, "Ogłoszenie");
            when(notificationConfigService.update(eq("OGLOSZENIE"), anyBoolean())).thenReturn(updated);

            mockMvc.perform(patch("/api/admin/notifications/settings/OGLOSZENIE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.enabled").value(false));
        }
    }
}

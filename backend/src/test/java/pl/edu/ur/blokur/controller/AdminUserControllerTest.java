package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import pl.edu.ur.blokur.dto.CreateUserRequest;
import pl.edu.ur.blokur.dto.UpdateUserRequest;
import pl.edu.ur.blokur.dto.UserResponse;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtAuthenticationFilter;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.AdminUserService;
import pl.edu.ur.blokur.service.LoginAttemptService;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminUserController — REST API zarządzania użytkownikami")
class AdminUserControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private AdminUserService adminUserService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private LoginAttemptService loginAttemptService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private UUID apartmentId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();
    }

    // -------------------------------------------------------
    // GET /api/admin/users
    // -------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/users — lista użytkowników")
    class GetAllUsers {

        @Test
        @DisplayName("Zwraca 200 z listą użytkowników")
        void shouldReturn200WithUserList() throws Exception {
            UserResponse resp = new UserResponse(
                    userId, "Jan", "Kowalski", "jan@blokur.pl", null, "MIESZKANIEC", true,
                    null, apartmentId);
            when(adminUserService.getAllUsers()).thenReturn(List.of(resp));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value("jan@blokur.pl"))
                    .andExpect(jsonPath("$[0].role").value("MIESZKANIEC"));
        }

        @Test
        @DisplayName("Zwraca 200 z pustą listą gdy brak użytkowników")
        void shouldReturn200WithEmptyList() throws Exception {
            when(adminUserService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // -------------------------------------------------------
    // POST /api/admin/users
    // -------------------------------------------------------

    @Nested
    @DisplayName("POST /api/admin/users — tworzenie użytkownika")
    class CreateUser {

        private CreateUserRequest validRequest;
        private User createdUser;

        @BeforeEach
        void setUpCreate() {
            validRequest = new CreateUserRequest();
            validRequest.setFirstName("Jan");
            validRequest.setLastName("Kowalski");
            validRequest.setEmail("jan@blokur.pl");
            validRequest.setRole("MIESZKANIEC");

            createdUser = new User();
            createdUser.setId(userId);
            createdUser.setFirstName("Jan");
            createdUser.setLastName("Kowalski");
            createdUser.setEmail("jan@blokur.pl");
            createdUser.setRole("MIESZKANIEC");
            createdUser.setActive(true);
        }

        @Test
        @DisplayName("Poprawne dane bez lokalu — zwraca 201 z danymi użytkownika")
        void shouldReturn201WithoutApartment() throws Exception {
            when(adminUserService.createUser(any())).thenReturn(createdUser);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("jan@blokur.pl"))
                    .andExpect(jsonPath("$.apartmentId").doesNotExist());
        }

        @Test
        @DisplayName("Poprawne dane z lokalem — zwraca 201 z apartmentId")
        void shouldReturn201WithApartmentId() throws Exception {
            Apartment apt = new Apartment();
            apt.setId(apartmentId);
            UserApartment ua = new UserApartment();
            ua.setApartment(apt);
            ua.setUser(createdUser);
            createdUser.getUserApartments().add(ua);

            when(adminUserService.createUser(any())).thenReturn(createdUser);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.apartmentId").value(apartmentId.toString()));
        }

        @Test
        @DisplayName("Zajęty email — zwraca 409")
        void shouldReturn409WhenEmailTaken() throws Exception {
            when(adminUserService.createUser(any()))
                    .thenThrow(new IllegalArgumentException("email jest już zajęty"));

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Nieistniejący lokal — zwraca 404")
        void shouldReturn404WhenApartmentNotFound() throws Exception {
            when(adminUserService.createUser(any()))
                    .thenThrow(new IllegalArgumentException("Lokal nie istnieje"));

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------
    // PATCH /api/admin/users/{id}
    // -------------------------------------------------------

    @Nested
    @DisplayName("PATCH /api/admin/users/{id} — aktualizacja użytkownika")
    class UpdateUser {

        private UpdateUserRequest updateRequest;
        private User updatedUser;

        @BeforeEach
        void setUpUpdate() {
            updateRequest = new UpdateUserRequest();
            updateRequest.setFirstName("Janusz");
            updateRequest.setLastName("Nowak");
            updateRequest.setRole("ZARZADCA");

            updatedUser = new User();
            updatedUser.setId(userId);
            updatedUser.setFirstName("Janusz");
            updatedUser.setLastName("Nowak");
            updatedUser.setEmail("jan@blokur.pl");
            updatedUser.setRole("ZARZADCA");
            updatedUser.setActive(true);
        }

        @Test
        @DisplayName("Poprawne dane — zwraca 200 z zaktualizowanym użytkownikiem")
        void shouldReturn200WhenUserUpdated() throws Exception {
            when(adminUserService.updateUser(eq(userId), any())).thenReturn(updatedUser);

            mockMvc.perform(patch("/api/admin/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Janusz"))
                    .andExpect(jsonPath("$.role").value("ZARZADCA"));
        }

        @Test
        @DisplayName("Poprawne dane z lokalem — zwraca 200 z apartmentId")
        void shouldReturn200WithApartmentId() throws Exception {
            Apartment apt = new Apartment();
            apt.setId(apartmentId);
            UserApartment ua = new UserApartment();
            ua.setApartment(apt);
            ua.setUser(updatedUser);
            updatedUser.getUserApartments().add(ua);

            when(adminUserService.updateUser(eq(userId), any())).thenReturn(updatedUser);

            mockMvc.perform(patch("/api/admin/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.apartmentId").value(apartmentId.toString()));
        }

        @Test
        @DisplayName("Nieistniejący użytkownik lub lokal — zwraca 404")
        void shouldReturn404WhenNotFound() throws Exception {
            when(adminUserService.updateUser(eq(userId), any()))
                    .thenThrow(new IllegalArgumentException("Użytkownik nie istnieje"));

            mockMvc.perform(patch("/api/admin/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------
    // PATCH /api/admin/users/{id}/deactivate
    // -------------------------------------------------------

    @Nested
    @DisplayName("PATCH /api/admin/users/{id}/deactivate — dezaktywacja konta")
    class DeactivateUser {

        @Test
        @DisplayName("Istniejący użytkownik — zwraca 204 bez treści")
        void shouldReturn204WhenDeactivated() throws Exception {
            mockMvc.perform(patch("/api/admin/users/" + userId + "/deactivate"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — zwraca 404 z komunikatem błędu")
        void shouldReturn404WithMessageWhenUserNotFound() throws Exception {
            doThrow(new IllegalArgumentException("Użytkownik nie istnieje"))
                    .when(adminUserService).deactivateUser(userId);

            mockMvc.perform(patch("/api/admin/users/" + userId + "/deactivate"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Użytkownik nie istnieje"));
        }
    }
}

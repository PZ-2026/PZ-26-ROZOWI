package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import pl.edu.ur.blokur.dto.PropertyRequest;
import pl.edu.ur.blokur.dto.PropertyResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.security.CustomUserDetailsService;
import pl.edu.ur.blokur.security.JwtService;
import pl.edu.ur.blokur.service.LoginAttemptService;
import pl.edu.ur.blokur.service.PropertyService;

/**
 * Testy jednostkowe kontrolera {@link PropertyController}. Weryfikują obsługę żądań HTTP, kody
 * odpowiedzi oraz walidację danych wejściowych na poziomie warstwy webowej.
 */
@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PropertyController — warstwa REST")
class PropertyControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private PropertyService propertyService;

    @MockitoBean private JwtService jwtService;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @MockitoBean private LoginAttemptService loginAttemptService;

    @MockitoBean private pl.edu.ur.blokur.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID propertyId;
    private PropertyRequest validRequest;
    private PropertyResponse propertyResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        propertyId = UUID.randomUUID();

        validRequest =
                new PropertyRequest(
                        "Wspólnota Słoneczna",
                        "ul. Kwiatowa 1, 35-000 Rzeszów",
                        "1234567890",
                        "123456789",
                        "zarzadca@blokur.pl");

        propertyResponse =
                new PropertyResponse(
                        propertyId,
                        "Wspólnota Słoneczna",
                        "ul. Kwiatowa 1, 35-000 Rzeszów",
                        "1234567890",
                        "123456789",
                        "zarzadca@blokur.pl",
                        null);

        // Zapobiega blokowaniu żądań przez zamockowany filtr JWT
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
    // POST /api/properties
    // =======================================================

    @Nested
    @DisplayName("POST /api/properties")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 201 Created")
        void shouldReturn201ForValidRequest() throws Exception {
            when(propertyService.create(any(PropertyRequest.class))).thenReturn(propertyResponse);

            mockMvc.perform(
                            post("/api/properties")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(propertyId.toString()))
                    .andExpect(jsonPath("$.name").value("Wspólnota Słoneczna"))
                    .andExpect(jsonPath("$.nip").value("1234567890"));
        }

        @Test
        @DisplayName("Brak pola name — zwraca 400 Bad Request")
        void shouldReturn400WhenNameMissing() throws Exception {
            PropertyRequest invalid =
                    new PropertyRequest(null, "ul. Kwiatowa 1", "1234567890", null, null);

            mockMvc.perform(
                            post("/api/properties")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Brak pola nip — zwraca 400 Bad Request")
        void shouldReturn400WhenNipMissing() throws Exception {
            PropertyRequest invalid =
                    new PropertyRequest("Wspólnota", "ul. Kwiatowa 1", null, null, null);

            mockMvc.perform(
                            post("/api/properties")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("NIP z nieprawidłowym formatem — zwraca 400 Bad Request")
        void shouldReturn400WhenNipInvalidFormat() throws Exception {
            PropertyRequest invalid =
                    new PropertyRequest("Wspólnota", "ul. Kwiatowa 1", "ABC123", null, null);

            mockMvc.perform(
                            post("/api/properties")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Duplikat NIP — zwraca 422 Unprocessable Entity")
        void shouldReturn422WhenNipDuplicate() throws Exception {
            when(propertyService.create(any(PropertyRequest.class)))
                    .thenThrow(
                            new BusinessValidationException(
                                    "Nieruchomość z NIP '1234567890' już istnieje"));

            mockMvc.perform(
                            post("/api/properties")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =======================================================
    // PUT /api/properties/{id}
    // =======================================================

    @Nested
    @DisplayName("PUT /api/properties/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca 200 OK z zaktualizowaną nieruchomością")
        void shouldReturn200ForValidUpdate() throws Exception {
            when(propertyService.update(eq(propertyId), any(PropertyRequest.class)))
                    .thenReturn(propertyResponse);

            mockMvc.perform(
                            put("/api/properties/{id}", propertyId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(propertyId.toString()));
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — zwraca 404 Not Found")
        void shouldReturn404WhenPropertyNotFound() throws Exception {
            when(propertyService.update(eq(propertyId), any(PropertyRequest.class)))
                    .thenThrow(
                            new NotFoundException(
                                    "Nieruchomość o ID " + propertyId + " nie istnieje"));

            mockMvc.perform(
                            put("/api/properties/{id}", propertyId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // =======================================================
    // GET /api/properties
    // =======================================================

    @Nested
    @DisplayName("GET /api/properties")
    class GetAllTests {

        @Test
        @DisplayName("Zwraca 200 z listą nieruchomości")
        void shouldReturn200WithList() throws Exception {
            when(propertyService.getAll()).thenReturn(List.of(propertyResponse));

            mockMvc.perform(get("/api/properties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].nip").value("1234567890"));
        }

        @Test
        @DisplayName("Brak nieruchomości — zwraca 200 z pustą listą")
        void shouldReturn200WithEmptyList() throws Exception {
            when(propertyService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/properties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =======================================================
    // GET /api/properties/{id}
    // =======================================================

    @Nested
    @DisplayName("GET /api/properties/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Istniejąca nieruchomość — zwraca 200 z danymi")
        void shouldReturn200WithProperty() throws Exception {
            when(propertyService.getById(propertyId)).thenReturn(propertyResponse);

            mockMvc.perform(get("/api/properties/{id}", propertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(propertyId.toString()))
                    .andExpect(jsonPath("$.name").value("Wspólnota Słoneczna"));
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — zwraca 404 Not Found")
        void shouldReturn404ForNonExistentProperty() throws Exception {
            when(propertyService.getById(propertyId))
                    .thenThrow(
                            new NotFoundException(
                                    "Nieruchomość o ID " + propertyId + " nie istnieje"));

            mockMvc.perform(get("/api/properties/{id}", propertyId))
                    .andExpect(status().isNotFound());
        }
    }

    // =======================================================
    // PATCH /api/properties/{id}/logo
    // =======================================================

    @Nested
    @DisplayName("PATCH /api/properties/{id}/logo")
    class UploadLogoTests {

        @Test
        @DisplayName("Plik PNG — zwraca 200 z zaktualizowaną ścieżką logo")
        void shouldReturn200ForPngUpload() throws Exception {
            PropertyResponse withLogo =
                    new PropertyResponse(
                            propertyId,
                            "Wspólnota Słoneczna",
                            "ul. Kwiatowa 1, 35-000 Rzeszów",
                            "1234567890",
                            "123456789",
                            "zarzadca@blokur.pl",
                            "/uploads/logos/" + propertyId + ".png");
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "logo.png", "image/png", new byte[] {(byte) 0x89, 0x50});
            when(propertyService.uploadLogo(eq(propertyId), any())).thenReturn(withLogo);

            MockMultipartHttpServletRequestBuilder request =
                    (MockMultipartHttpServletRequestBuilder)
                            multipart("/api/properties/{id}/logo", propertyId)
                                    .file(file)
                                    .with(
                                            r -> {
                                                r.setMethod("PATCH");
                                                return r;
                                            });

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.logoPath").value("/uploads/logos/" + propertyId + ".png"));
        }

        @Test
        @DisplayName("Niedozwolony typ pliku — zwraca 422 Unprocessable Entity")
        void shouldReturn422ForInvalidFileType() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "logo.gif", "image/gif", new byte[] {0x01});
            when(propertyService.uploadLogo(eq(propertyId), any()))
                    .thenThrow(
                            new BusinessValidationException("Logo musi być plikiem PNG lub JPEG"));

            MockMultipartHttpServletRequestBuilder request =
                    (MockMultipartHttpServletRequestBuilder)
                            multipart("/api/properties/{id}/logo", propertyId)
                                    .file(file)
                                    .with(
                                            r -> {
                                                r.setMethod("PATCH");
                                                return r;
                                            });

            mockMvc.perform(request).andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — zwraca 404 Not Found")
        void shouldReturn404WhenPropertyNotFound() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile("file", "logo.png", "image/png", new byte[] {0x01});
            when(propertyService.uploadLogo(eq(propertyId), any()))
                    .thenThrow(
                            new NotFoundException(
                                    "Nieruchomość o ID " + propertyId + " nie istnieje"));

            MockMultipartHttpServletRequestBuilder request =
                    (MockMultipartHttpServletRequestBuilder)
                            multipart("/api/properties/{id}/logo", propertyId)
                                    .file(file)
                                    .with(
                                            r -> {
                                                r.setMethod("PATCH");
                                                return r;
                                            });

            mockMvc.perform(request).andExpect(status().isNotFound());
        }
    }
}

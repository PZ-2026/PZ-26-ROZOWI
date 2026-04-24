package pl.edu.ur.blokur.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.service.DocumentService;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private DocumentService documentService;

    @MockitoBean private pl.edu.ur.blokur.security.JwtService jwtService;

    @MockitoBean
    private pl.edu.ur.blokur.security.CustomUserDetailsService customUserDetailsService;

    @MockitoBean private pl.edu.ur.blokur.service.LoginAttemptService loginAttemptService;

    @MockitoBean private pl.edu.ur.blokur.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private DocumentDto documentDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        documentDto =
                new DocumentDto(
                        UUID.randomUUID(),
                        "PROTOKOL",
                        "Protokół odbioru",
                        LocalDateTime.now(),
                        "/api/documents/123/download");

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

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void getDocuments_ShouldReturnList() throws Exception {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(documentService.getDocuments(any(), any(), any(), any(), any()))
                .thenReturn(List.of(documentDto));

        mockMvc.perform(get("/api/documents").principal(() -> mockUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Protokół odbioru"))
                .andExpect(jsonPath("$[0].type").value("PROTOKOL"));
    }

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void getDocuments_ShouldReturnForbidden_WhenSecurityException() throws Exception {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(documentService.getDocuments(any(), any(), any(), any(), any()))
                .thenThrow(new SecurityException("Forbidden"));

        mockMvc.perform(get("/api/documents").principal(() -> mockUserId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void downloadDocument_ShouldReturnFile() throws Exception {
        UUID mockUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID docId = UUID.randomUUID();

        Resource mockResource =
                new ByteArrayResource("PDF Content".getBytes()) {
                    @Override
                    public String getFilename() {
                        return "test.pdf";
                    }
                };

        when(documentService.downloadDocument(docId, mockUserId)).thenReturn(mockResource);

        mockMvc.perform(
                        get("/api/documents/{id}/download", docId)
                                .principal(() -> mockUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(
                        header().string("Content-Disposition", "attachment; filename=\"test.pdf\""))
                .andExpect(content().string("PDF Content"));
    }
}

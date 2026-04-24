package pl.edu.ur.blokur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.DocumentRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private DocumentService documentService;

    private User admin;
    private User resident;
    private Apartment apartment;
    private Document document;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole("ZARZADCA");

        resident = new User();
        resident.setId(UUID.randomUUID());
        resident.setRole("MIESZKANIEC");

        apartment = new Apartment();
        apartment.setId(UUID.randomUUID());

        UserApartment ua = new UserApartment();
        ua.setApartment(apartment);
        ua.setUser(resident);
        resident.setUserApartments(List.of(ua));

        Path tempFile = tempDir.resolve("test.pdf");
        java.nio.file.Files.write(tempFile, "dummy content".getBytes());

        document = new Document();
        document.setId(UUID.randomUUID());
        document.setType("PROTOKOL");
        document.setTitle("Protokół testowy");
        document.setCreatedAt(LocalDateTime.now());
        document.setApartment(apartment);
        document.setFileUrl(tempFile.toAbsolutePath().toString());
    }

    @Test
    void getDocuments_Admin_ReturnsAllDocuments() {
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(documentRepository.findAllWithFilters(any(), any(), any(), any()))
                .thenReturn(List.of(document));

        List<DocumentDto> result =
                documentService.getDocuments(null, null, null, null, admin.getId());

        assertEquals(1, result.size());
        verify(documentRepository, times(1)).findAllWithFilters(any(), any(), any(), any());
    }

    @Test
    void getDocuments_Resident_ReturnsOwnedDocuments() {
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(documentRepository.findByApartmentIdOrOwnerUserId(null, resident.getId()))
                .thenReturn(List.of(document));

        List<DocumentDto> result =
                documentService.getDocuments(null, null, null, null, resident.getId());

        assertEquals(1, result.size());
        verify(documentRepository, times(1)).findByApartmentIdOrOwnerUserId(null, resident.getId());
    }

    @Test
    void getDocuments_Resident_AccessDeniedForOtherApartment() {
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertThrows(
                SecurityException.class,
                () ->
                        documentService.getDocuments(
                                UUID.randomUUID(), null, null, null, resident.getId()));
    }

    @Test
    void downloadDocument_Admin_Success() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        Resource resource = documentService.downloadDocument(document.getId(), admin.getId());

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void downloadDocument_Resident_Success_IfApartmentMatches() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        Resource resource = documentService.downloadDocument(document.getId(), resident.getId());

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void downloadDocument_Resident_Forbidden_IfNoMatchingApartment() {
        User otherResident = new User();
        otherResident.setId(UUID.randomUUID());
        otherResident.setRole("MIESZKANIEC");

        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(otherResident.getId())).thenReturn(Optional.of(otherResident));

        assertThrows(
                SecurityException.class,
                () -> documentService.downloadDocument(document.getId(), otherResident.getId()));
    }
}

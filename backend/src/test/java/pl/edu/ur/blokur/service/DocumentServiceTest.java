package pl.edu.ur.blokur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.DocumentRepository;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.storage.DocumentStorage;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;

    @Mock private UserRepository userRepository;

    @Mock private DocumentStorage documentStorage;

    @InjectMocks private DocumentService documentService;

    private User admin;
    private User resident;
    private Apartment apartment;
    private Document document;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

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

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
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
        Resource stubbed =
                new org.springframework.core.io.FileSystemResource(document.getFileUrl());
        when(documentStorage.load(document.getFileUrl())).thenReturn(stubbed);

        Resource resource = documentService.downloadDocument(document.getId(), admin.getId());

        assertNotNull(resource);
        assertTrue(resource.exists());
        verify(documentStorage).load(document.getFileUrl());
    }

    @Test
    void downloadDocument_Resident_Success_IfApartmentMatches() {
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        Resource stubbed =
                new org.springframework.core.io.FileSystemResource(document.getFileUrl());
        when(documentStorage.load(document.getFileUrl())).thenReturn(stubbed);

        Resource resource = documentService.downloadDocument(document.getId(), resident.getId());

        assertNotNull(resource);
        assertTrue(resource.exists());
        verify(documentStorage).load(document.getFileUrl());
    }

    @Test
    void storeGeneratedDocument_PersistsEntityAndDelegatesToStorage() {
        byte[] pdfBytes = "%PDF-1.4 test".getBytes();
        String expectedUrl = tempDir.resolve("documents").resolve("stub.pdf").toString();
        when(documentStorage.store(eq("documents"), org.mockito.ArgumentMatchers.anyString(),
                        eq(pdfBytes)))
                .thenReturn(expectedUrl);
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        Document saved =
                documentService.storeGeneratedDocument(
                        "PROTOKOL",
                        "Protokół odbioru ZGL-2026-0001",
                        pdfBytes,
                        admin,
                        apartment,
                        null,
                        null);

        assertNotNull(saved);
        assertEquals("PROTOKOL", saved.getType());
        assertEquals("Protokół odbioru ZGL-2026-0001", saved.getTitle());
        assertEquals(expectedUrl, saved.getFileUrl());
        assertEquals(admin, saved.getOwnerUser());
        assertEquals(apartment, saved.getApartment());
        verify(documentStorage)
                .store(eq("documents"), org.mockito.ArgumentMatchers.anyString(), eq(pdfBytes));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void storeGeneratedDocument_SanitizesPolishDiacriticsInFileName() {
        byte[] pdfBytes = "%PDF".getBytes();
        when(documentStorage.store(eq("documents"), org.mockito.ArgumentMatchers.anyString(),
                        any(byte[].class)))
                .thenAnswer(inv -> "documents/" + inv.getArgument(1, String.class));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        Document saved =
                documentService.storeGeneratedDocument(
                        "RAPORT_SALD",
                        "Zestawienie zaległości — łąka",
                        pdfBytes,
                        admin,
                        null,
                        null,
                        null);

        // Sprawdza że ścieżka nie zawiera polskich znaków ani spacji
        String url = saved.getFileUrl();
        assertTrue(url.matches("documents/raport_sald-[a-z0-9-]+-\\d+\\.pdf"),
                "Niespodziewany format URL: " + url);
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

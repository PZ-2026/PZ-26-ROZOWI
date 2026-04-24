package pl.edu.ur.blokur.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.DocumentRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/** Serwis zarządzający dokumentami i obsługą pobierania plików PDF. */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /**
     * Konstruktor serwisu dokumentów.
     *
     * @param documentRepository repozytorium dokumentów
     * @param userRepository repozytorium użytkowników
     */
    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Zwraca listę dokumentów przefiltrowanych zgodnie z rolą użytkownika. ZARZADCA widzi wszystkie
     * (możliwość filtrowania wg wszystkich parametrów). MIESZKANIEC widzi dokumenty powiązane z
     * jego własnym kontem (ownerUserId) lub wskazanym apartmentId (musi on należeć do mieszkańca).
     *
     * @param apartmentId opcjonalny identyfikator mieszkania
     * @param type opcjonalny typ dokumentu
     * @param startDate początek zakresu dat (data)
     * @param endDate koniec zakresu dat (data)
     * @param userId identyfikator użytkownika wykonującego żądanie
     * @return lista obiektów DocumentDto
     */
    public List<DocumentDto> getDocuments(
            UUID apartmentId, LocalDate startDate, LocalDate endDate, String type, UUID userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<Document> documents;

        if ("ZARZADCA".equals(user.getRole())) {
            documents =
                    documentRepository.findAllWithFilters(
                            apartmentId, type, startDateTime, endDateTime);
        } else {
            // MIESZKANIEC
            if (apartmentId != null && !isApartmentOwnedByUser(user, apartmentId)) {
                throw new SecurityException("Brak dostępu do podanego mieszkania");
            }
            documents =
                    documentRepository.findByApartmentIdOrOwnerUserId(apartmentId, user.getId());
            // Zastosowanie dodatkowych filtrów w pamięci dla MIESZKAŃCA (jeśli by podał np. typ i
            // daty)
            if (type != null) {
                documents = documents.stream().filter(d -> type.equals(d.getType())).toList();
            }
            if (startDateTime != null) {
                documents =
                        documents.stream()
                                .filter(d -> !d.getCreatedAt().isBefore(startDateTime))
                                .toList();
            }
            if (endDateTime != null) {
                documents =
                        documents.stream()
                                .filter(d -> !d.getCreatedAt().isAfter(endDateTime))
                                .toList();
            }
        }

        return documents.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Zwraca zasób reprezentujący pobierany plik z dysku, po weryfikacji uprawnień użytkownika.
     *
     * @param documentId identyfikator dokumentu
     * @param userId identyfikator użytkownika
     * @return zasób (Resource) z plikiem PDF
     */
    public Resource downloadDocument(UUID documentId, UUID userId) {
        Document document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono dokumentu"));

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        if (!"ZARZADCA".equals(user.getRole())) {
            boolean isOwner =
                    document.getOwnerUser() != null
                            && document.getOwnerUser().getId().equals(user.getId());
            boolean isApartmentOwner =
                    document.getApartment() != null
                            && isApartmentOwnedByUser(user, document.getApartment().getId());

            if (!isOwner && !isApartmentOwner) {
                throw new SecurityException("Brak dostępu do pobrania tego dokumentu");
            }
        }

        Path filePath = Paths.get(document.getFileUrl());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("Plik nie istnieje lub jest nieczytelny na dysku serwera");
        }

        return resource;
    }

    private boolean isApartmentOwnedByUser(User user, UUID apartmentId) {
        return user.getUserApartments().stream()
                .anyMatch(ua -> ua.getApartment().getId().equals(apartmentId));
    }

    private DocumentDto mapToDto(Document document) {
        String downloadUrl =
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/documents/")
                        .path(document.getId().toString())
                        .path("/download")
                        .toUriString();

        return new DocumentDto(
                document.getId(),
                document.getType(),
                document.getTitle(),
                document.getCreatedAt(),
                downloadUrl);
    }
}

package pl.edu.ur.blokur.service;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
    public List<DocumentDto> getDocuments(
            UUID apartmentId, LocalDate startDate, LocalDate endDate, String type, UUID userId) {
        var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        var startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        var endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        if ("ZARZADCA".equals(user.getRole())) {
            return documentRepository
                    .findAllWithFilters(apartmentId, type, startDateTime, endDateTime)
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }

        if (apartmentId != null && !isApartmentOwnedByUser(user, apartmentId)) {
            throw new SecurityException("Brak dostępu do podanego mieszkania");
        }

        return documentRepository.findByApartmentIdOrOwnerUserId(apartmentId, user.getId()).stream()
                .filter(d -> type == null || type.equals(d.getType()))
                .filter(d -> startDateTime == null || !d.getCreatedAt().isBefore(startDateTime))
                .filter(d -> endDateTime == null || !d.getCreatedAt().isAfter(endDateTime))
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Zwraca zasób reprezentujący pobierany plik z dysku, po weryfikacji uprawnień użytkownika.
     *
     * @param documentId identyfikator dokumentu
     * @param userId identyfikator użytkownika
     * @return zasób (Resource) z plikiem PDF
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID documentId, UUID userId) {
        var document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono dokumentu"));

        var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        if (!"ZARZADCA".equals(user.getRole())) {
            var isOwner =
                    document.getOwnerUser() != null
                            && document.getOwnerUser().getId().equals(userId);

            var isApartmentOwner =
                    document.getApartment() != null
                            && isApartmentOwnedByUser(user, document.getApartment().getId());

            if (!isOwner && !isApartmentOwner) {
                throw new SecurityException("Brak dostępu do pobrania tego dokumentu");
            }
        }

        var filePath = Paths.get(document.getFileUrl()).normalize();
        var resource = new FileSystemResource(filePath);

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

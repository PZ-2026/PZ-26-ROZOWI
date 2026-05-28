package pl.edu.ur.blokur.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.edu.ur.blokur.dto.DocumentDto;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Document;
import pl.edu.ur.blokur.models.Resolution;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.DocumentRepository;
import pl.edu.ur.blokur.repository.UserRepository;
import pl.edu.ur.blokur.service.storage.DocumentStorage;

/**
 * Serwis zarządzający dokumentami: tworzenie wpisów w tabeli {@code documents} powiązanych z
 * fizycznym plikiem w {@link DocumentStorage}, listowanie z kontrolą dostępu oraz pobieranie do
 * downloadu.
 */
@Service
public class DocumentService {

    /** Domyślny podkatalog w storage dla dokumentów generowanych przez system. */
    public static final String DOCUMENTS_SUBDIR = "documents";

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentStorage documentStorage;

    /**
     * Konstruktor serwisu dokumentów.
     *
     * @param documentRepository repozytorium dokumentów
     * @param userRepository repozytorium użytkowników
     * @param documentStorage warstwa fizycznego zapisu plików (LocalDisk lub S3)
     */
    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            DocumentStorage documentStorage) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentStorage = documentStorage;
    }

    /**
     * Zapisuje wygenerowany plik PDF w storage i tworzy odpowiadający mu wpis w tabeli {@code
     * documents}. Powiązania z lokalem, zgłoszeniem i uchwałą są opcjonalne (mogą być {@code
     * null}) — to elastyczne klucze obce zgodnie z opisem kryteriów akceptacji.
     *
     * @param type typ dokumentu (np. {@code PROTOKOL}, {@code RAPORT_SALD}, {@code UCHWALA})
     * @param title czytelny tytuł dokumentu (pokazywany użytkownikowi)
     * @param pdfBytes zawartość pliku PDF
     * @param ownerUser użytkownik będący właścicielem dokumentu (np. zarządca generujący raport)
     * @param apartment opcjonalne powiązanie z lokalem
     * @param ticket opcjonalne powiązanie ze zgłoszeniem
     * @param resolution opcjonalne powiązanie z uchwałą
     * @return zapisana encja {@link Document} z wygenerowanym ID i ścieżką do pliku
     */
    @Transactional
    public Document storeGeneratedDocument(
            String type,
            String title,
            byte[] pdfBytes,
            User ownerUser,
            Apartment apartment,
            Ticket ticket,
            Resolution resolution) {
        var fileName = buildFileName(type, title);
        var fileUrl = documentStorage.store(DOCUMENTS_SUBDIR, fileName, pdfBytes);

        var document = new Document();
        document.setType(type);
        document.setTitle(title);
        document.setFileUrl(fileUrl);
        document.setOwnerUser(ownerUser);
        document.setApartment(apartment);
        document.setTicket(ticket);
        document.setResolution(resolution);
        return documentRepository.save(document);
    }

    /**
     * Buduje unikalną nazwę pliku PDF: {@code <typ-lowercase>-<sanitized-title>-<timestamp>.pdf}.
     *
     * @param type typ dokumentu (prefix)
     * @param title tytuł dokumentu (zostanie zsanityzowany — bez polskich znaków i znaków
     *     specjalnych)
     * @return nazwa pliku do użycia w storage
     */
    private static String buildFileName(String type, String title) {
        var prefix = (type != null ? type : "doc").toLowerCase();
        var sanitizedTitle =
                title == null
                        ? "untitled"
                        : title.toLowerCase()
                                .replaceAll("[ąáàäâãå]", "a")
                                .replaceAll("[ćç]", "c")
                                .replaceAll("[ęéèëê]", "e")
                                .replaceAll("[ł]", "l")
                                .replaceAll("[ńñ]", "n")
                                .replaceAll("[óöòôõø]", "o")
                                .replaceAll("[śš]", "s")
                                .replaceAll("[üúùû]", "u")
                                .replaceAll("[ýÿ]", "y")
                                .replaceAll("[źżž]", "z")
                                .replaceAll("[^a-z0-9]+", "-")
                                .replaceAll("^-+|-+$", "");
        if (sanitizedTitle.isEmpty()) {
            sanitizedTitle = "untitled";
        }
        if (sanitizedTitle.length() > 80) {
            sanitizedTitle = sanitizedTitle.substring(0, 80);
        }
        return prefix + "-" + sanitizedTitle + "-" + System.currentTimeMillis() + ".pdf";
    }

    /**
     * Zwraca listę dokumentów przefiltrowanych zgodnie z rolą użytkownika. ZARZADCA widzi
     * wszystkie (możliwość filtrowania wg wszystkich parametrów). MIESZKANIEC widzi dokumenty
     * powiązane z jego własnym kontem (ownerUserId) lub wskazanym apartmentId (musi on należeć do
     * mieszkańca).
     *
     * @param apartmentId opcjonalny identyfikator mieszkania
     * @param type opcjonalny typ dokumentu
     * @param startDate początek zakresu dat (data)
     * @param endDate koniec zakresu dat (data)
     * @param username email zalogowanego użytkownika
     * @return lista obiektów DocumentDto
     */
    @Transactional(readOnly = true)
    public List<DocumentDto> getDocuments(
            UUID apartmentId, LocalDate startDate, LocalDate endDate, String type, String username) {
        var user =
                userRepository
                        .findByEmail(username)
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

        return documentRepository.findByApartmentIdOrOwnerUserId(apartmentId, user.getId())
                .stream()
                .filter(d -> type == null || type.equals(d.getType()))
                .filter(d -> startDateTime == null || !d.getCreatedAt().isBefore(startDateTime))
                .filter(d -> endDateTime == null || !d.getCreatedAt().isAfter(endDateTime))
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Zwraca zasób reprezentujący pobierany plik ze storage, po weryfikacji uprawnień użytkownika.
     *
     * @param documentId identyfikator dokumentu
     * @param username email zalogowanego użytkownika
     * @return zasób (Resource) z plikiem PDF
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID documentId, String username) {
        var document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono dokumentu"));

        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        if (!"ZARZADCA".equals(user.getRole())) {
            var isOwner =
                    document.getOwnerUser() != null
                            && document.getOwnerUser().getId().equals(user.getId());

            var isApartmentOwner =
                    document.getApartment() != null
                            && isApartmentOwnedByUser(user, document.getApartment().getId());

            if (!isOwner && !isApartmentOwner) {
                throw new SecurityException("Brak dostępu do pobrania tego dokumentu");
            }
        }

        return documentStorage.load(document.getFileUrl());
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

package pl.edu.ur.blokur.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.AnnouncementDto;
import pl.edu.ur.blokur.dto.AnnouncementRequest;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Announcement;
import pl.edu.ur.blokur.models.AnnouncementTargetType;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.AnnouncementRepository;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.StaircaseRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis dostarczający logikę biznesową dla modułu ogłoszeń. Obsługuje operacje CRUD oraz
 * pobieranie ogłoszeń dla zalogowanego użytkownika.
 */
@Service
public class AnnouncementService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final StaircaseRepository staircaseRepository;
    private final ApartmentRepository apartmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Tworzy instancję serwisu z wymaganymi repozytoriami.
     *
     * @param announcementRepository repozytorium ogłoszeń
     * @param userRepository repozytorium użytkowników
     * @param buildingRepository repozytorium budynków
     * @param staircaseRepository repozytorium klatek
     * @param apartmentRepository repozytorium lokali
     */
    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            UserRepository userRepository,
            BuildingRepository buildingRepository,
            StaircaseRepository staircaseRepository,
            ApartmentRepository apartmentRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.buildingRepository = buildingRepository;
        this.staircaseRepository = staircaseRepository;
        this.apartmentRepository = apartmentRepository;
    }

    /**
     * Zwraca listę ogłoszeń dopasowanych do zalogowanego użytkownika. Ogłoszenia starsze niż 12
     * miesięcy nie są zwracane.
     *
     * @param username adres email zalogowanego użytkownika
     * @return lista ogłoszeń w formie DTO
     */
    public List<AnnouncementDto> getAnnouncementsForUser(String username) {
        User user = userRepository.findByEmail(username).orElse(null);

        UUID buildingId = null;
        UUID staircaseId = null;
        UUID apartmentId = null;

        if (user != null && !user.getUserApartments().isEmpty()) {
            UserApartment ua = user.getUserApartments().get(0);
            Apartment apt = ua.getApartment();
            if (apt != null) {
                apartmentId = apt.getId();
                Staircase sc = apt.getStaircase();
                if (sc != null) {
                    staircaseId = sc.getId();
                    Building b = sc.getBuilding();
                    if (b != null) {
                        buildingId = b.getId();
                    }
                }
            }
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMonths(12);

        List<Announcement> list =
                announcementRepository.findForUserAfterDate(
                        buildingId, staircaseId, apartmentId, cutoff);

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Tworzy nowe ogłoszenie. Dostępne tylko dla zarządcy.
     *
     * @param request dane ogłoszenia
     * @param attachment opcjonalny załącznik PDF
     * @param username email zarządcy
     * @return utworzone ogłoszenie jako DTO
     */
    @Transactional
    public AnnouncementDto createAnnouncement(
            AnnouncementRequest request, MultipartFile attachment, String username) {
        User author =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(author.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może tworzyć ogłoszenia.");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAuthor(author);
        announcement.setTargetType(request.getTargetType());
        announcement.setPlannedDate(request.getPlannedDate());

        assignTarget(announcement, request.getTargetType(), request.getTargetId());

        if (attachment != null && !attachment.isEmpty()) {
            String path = saveAttachment(attachment);
            announcement.setAttachmentPath(path);
        }

        Announcement saved = announcementRepository.save(announcement);

        sendPushNotificationsAsync(saved);

        return mapToDto(saved);
    }

    /**
     * Aktualizuje istniejące ogłoszenie. Dostępne tylko dla zarządcy.
     *
     * @param announcementId identyfikator ogłoszenia
     * @param request zaktualizowane dane
     * @param attachment opcjonalny nowy załącznik PDF
     * @param username email zarządcy
     * @return zaktualizowane ogłoszenie jako DTO
     */
    @Transactional
    public AnnouncementDto updateAnnouncement(
            UUID announcementId,
            AnnouncementRequest request,
            MultipartFile attachment,
            String username) {
        Announcement announcement =
                announcementRepository
                        .findById(announcementId)
                        .orElseThrow(() -> new NotFoundException("Ogłoszenie nie istnieje"));

        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(user.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może edytować ogłoszenia.");
        }

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetType(request.getTargetType());
        announcement.setPlannedDate(request.getPlannedDate());

        // Wyczyść stare powiązania
        announcement.setTargetBuilding(null);
        announcement.setTargetStaircase(null);
        announcement.setTargetApartment(null);

        assignTarget(announcement, request.getTargetType(), request.getTargetId());

        if (attachment != null && !attachment.isEmpty()) {
            String path = saveAttachment(attachment);
            announcement.setAttachmentPath(path);
        }

        return mapToDto(announcementRepository.save(announcement));
    }

    /**
     * Usuwa ogłoszenie. Dostępne tylko dla zarządcy.
     *
     * @param announcementId identyfikator ogłoszenia
     * @param username email zarządcy
     */
    @Transactional
    public void deleteAnnouncement(UUID announcementId, String username) {
        Announcement announcement =
                announcementRepository
                        .findById(announcementId)
                        .orElseThrow(() -> new NotFoundException("Ogłoszenie nie istnieje"));

        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if (!"ZARZADCA".equals(user.getRole())) {
            throw new BusinessValidationException(
                    "Brak uprawnień. Tylko zarządca może usuwać ogłoszenia.");
        }

        announcementRepository.delete(announcement);
    }

    /** Przypisuje cel ogłoszenia na podstawie typu zasięgu. */
    private void assignTarget(
            Announcement announcement, AnnouncementTargetType targetType, UUID targetId) {
        switch (targetType) {
            case BUDYNEK:
                if (targetId == null) {
                    throw new BusinessValidationException(
                            "Wymagany identyfikator budynku dla typu BUDYNEK");
                }
                Building building =
                        buildingRepository
                                .findById(targetId)
                                .orElseThrow(() -> new NotFoundException("Budynek nie istnieje"));
                announcement.setTargetBuilding(building);
                break;
            case KLATKA:
                if (targetId == null) {
                    throw new BusinessValidationException(
                            "Wymagany identyfikator klatki dla typu KLATKA");
                }
                Staircase staircase =
                        staircaseRepository
                                .findById(targetId)
                                .orElseThrow(() -> new NotFoundException("Klatka nie istnieje"));
                announcement.setTargetStaircase(staircase);
                break;
            case NIERUCHOMOSC:
                if (targetId == null) {
                    throw new BusinessValidationException(
                            "Wymagany identyfikator lokalu dla typu NIERUCHOMOSC");
                }
                Apartment apartment =
                        apartmentRepository
                                .findById(targetId)
                                .orElseThrow(() -> new NotFoundException("Lokal nie istnieje"));
                announcement.setTargetApartment(apartment);
                break;
            case WSZYSCY:
            default:
                break;
        }
    }

    /**
     * Zapisuje załącznik PDF na dysku.
     *
     * @param file plik do zapisania
     * @return ścieżka do zapisanego pliku
     */
    private String saveAttachment(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BusinessValidationException("Załącznik musi być plikiem PDF");
        }

        try {
            Path dirPath = Paths.get(uploadDir, "announcements");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, file.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisu załącznika", e);
        }
    }

    /**
     * Wysyła powiadomienia PUSH do adresatów ogłoszenia (asynchronicznie). Obecnie loguje
     * informację — implementacja PUSH wymaga integracji z FCM/APNs.
     *
     * @param announcement zapisane ogłoszenie
     */
    @Async
    public void sendPushNotificationsAsync(Announcement announcement) {
        logger.info(
                "Wysyłanie powiadomień PUSH dla ogłoszenia: {} (typ zasięgu: {})",
                announcement.getId(),
                announcement.getTargetType());
        // TODO: Integracja z Firebase Cloud Messaging lub Apple Push Notification Service
    }

    /**
     * Mapuje encję ogłoszenia na obiekt DTO.
     *
     * @param a encja ogłoszenia do zmapowania
     * @return obiekt DTO gotowy do serializacji
     */
    private AnnouncementDto mapToDto(Announcement a) {
        String authorName = "";
        if (a.getAuthor() != null) {
            authorName = a.getAuthor().getFirstName() + " " + a.getAuthor().getLastName();
        }

        String attachmentUrl = null;
        if (a.getAttachmentPath() != null) {
            attachmentUrl = "/api/announcements/" + a.getId() + "/attachment";
        }

        return new AnnouncementDto(
                a.getId(),
                a.getType(),
                a.getTitle(),
                a.getContent(),
                authorName,
                a.getTargetType() != null ? a.getTargetType().name() : null,
                attachmentUrl,
                a.getPlannedDate(),
                a.getCreatedAt());
    }
}

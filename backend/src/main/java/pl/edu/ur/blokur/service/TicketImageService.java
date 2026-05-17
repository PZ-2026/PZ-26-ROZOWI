package pl.edu.ur.blokur.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.edu.ur.blokur.dto.TicketImageDto;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketImage;
import pl.edu.ur.blokur.models.TicketImageType;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.TicketImageRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/** Serwis zarządzający zdjęciami powiązanymi ze zgłoszeniami. */
@Service
public class TicketImageService {

    private final TicketImageRepository ticketImageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final String uploadDir;

    public TicketImageService(
            TicketImageRepository ticketImageRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.ticketImageRepository = ticketImageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.uploadDir = uploadDir;
    }

    /**
     * Zapisuje przesłane zdjęcie zgłoszenia na dysku i dodaje rekord do bazy.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param file przesyłany plik (multipart)
     * @param imageType typ zdjęcia (BEFORE lub AFTER)
     * @param username nazwa użytkownika przesyłającego
     * @return DTO utworzonego zdjęcia
     */
    @Transactional
    public TicketImageDto uploadImage(
            UUID ticketId, MultipartFile file, TicketImageType imageType, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono zgłoszenia"));
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        validateUploadAccess(ticket, user, imageType);
        validateFile(file);

        long beforeCount =
                ticket.getImages().stream()
                        .filter(img -> img.getImageType() == TicketImageType.BEFORE)
                        .count();
        long afterCount =
                ticket.getImages().stream()
                        .filter(img -> img.getImageType() == TicketImageType.AFTER)
                        .count();

        if (imageType == TicketImageType.BEFORE && beforeCount >= 5) {
            throw new BusinessValidationException(
                    "Osiągnięto limit 5 zdjęć przed naprawą (BEFORE).");
        }
        if (imageType == TicketImageType.AFTER && afterCount >= 10) {
            throw new BusinessValidationException("Osiągnięto limit 10 zdjęć po naprawie (AFTER).");
        }

        try {
            var ticketUploadDir = Paths.get(uploadDir, "tickets", ticketId.toString());
            if (!Files.exists(ticketUploadDir)) {
                Files.createDirectories(ticketUploadDir);
            }

            var originalFilename =
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            var savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            var filePath = ticketUploadDir.resolve(savedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            TicketImage ticketImage = new TicketImage();
            ticketImage.setTicket(ticket);
            ticketImage.setUploader(user);
            ticketImage.setImageType(imageType);
            ticketImage.setFilePath(filePath.toAbsolutePath().toString());
            ticketImage.setOriginalFilename(originalFilename);
            ticketImage.setUploadedAt(LocalDateTime.now());

            var savedImage = ticketImageRepository.save(ticketImage);
            return mapToDto(savedImage);

        } catch (IOException e) {
            throw new BusinessValidationException(
                    "Błąd podczas zapisu pliku na serwerze: " + e.getMessage());
        }
    }

    /**
     * Zwraca listę zdjęć dla podanego zgłoszenia.
     *
     * @param ticketId identyfikator zgłoszenia
     * @param username nazwa użytkownika żądającego
     * @return lista DTO zdjęć
     */
    public List<TicketImageDto> getImagesForTicket(UUID ticketId, String username) {
        var ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono zgłoszenia"));
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        validateViewAccess(ticket, user);

        return ticketImageRepository.findByTicketIdOrderByUploadedAtAsc(ticketId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Serwuje fizyczny plik obrazu z dysku.
     *
     * @param imageId identyfikator obrazu
     * @param username nazwa użytkownika żądającego
     * @return Zasób pliku
     */
    public Resource serveImage(UUID imageId, String username) {
        var image =
                ticketImageRepository
                        .findById(imageId)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono obrazu"));
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono użytkownika"));

        validateViewAccess(image.getTicket(), user);

        var filePath = Paths.get(image.getFilePath());
        var resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("Plik nie istnieje lub jest nieczytelny na dysku serwera");
        }

        return resource;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessValidationException("Plik jest pusty.");
        }
        long maxSizeBytes = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessValidationException("Rozmiar pliku przekracza maksymalne 10 MB.");
        }
        var contentType = file.getContentType();
        if (contentType == null
                || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BusinessValidationException(
                    "Niedozwolony format pliku. Obsługiwane są tylko image/jpeg oraz image/png.");
        }
    }

    private void validateUploadAccess(Ticket ticket, User user, TicketImageType type) {
        if ("ZARZADCA".equals(user.getRole())) {
            return;
        }

        switch (type) {
            case BEFORE -> {
                if (!"MIESZKANIEC".equals(user.getRole())) {
                    throw new BusinessValidationException(
                            "Tylko mieszkaniec może dodawać zdjęcia BEFORE.");
                }
                if (!ticket.getAuthor().getId().equals(user.getId())) {
                    throw new BusinessValidationException("Brak dostępu do zgłoszenia.");
                }
            }
            case AFTER -> {
                if (!"KONSERWATOR".equals(user.getRole())) {
                    throw new BusinessValidationException(
                            "Tylko konserwator może dodawać zdjęcia AFTER.");
                }
                if (ticket.getAssignedTo() == null
                        || !ticket.getAssignedTo().getId().equals(user.getId())) {
                    throw new BusinessValidationException(
                            "Zgłoszenie nie jest przypisane do tego konserwatora.");
                }
            }
        }
    }

    private void validateViewAccess(Ticket ticket, User user) {
        switch (user.getRole()) {
            case "ZARZADCA" -> {}
            case "MIESZKANIEC" -> {
                if (!ticket.getAuthor().getId().equals(user.getId())) {
                    throw new SecurityException("Brak dostępu do cudzego zgłoszenia.");
                }
            }
            case "KONSERWATOR" -> {
                if (ticket.getAssignedTo() == null
                        || !ticket.getAssignedTo().getId().equals(user.getId())) {
                    throw new SecurityException(
                            "Zgłoszenie nie jest przypisane do tego konserwatora.");
                }
            }
            default -> {
                throw new SecurityException("Odmowa dostępu: nieznana rola lub brak uprawnień.");
            }
        }
    }

    private TicketImageDto mapToDto(TicketImage image) {
        String url = "";
        try {
            url =
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/images/")
                            .path(image.getId().toString())
                            .toUriString();
        } catch (Exception e) {
            // Ignorowane, np. podczas testów bez kontekstu
        }

        return new TicketImageDto(
                image.getId(),
                image.getTicket().getId(),
                image.getUploader().getId(),
                image.getImageType(),
                image.getOriginalFilename(),
                image.getUploadedAt(),
                url);
    }
}

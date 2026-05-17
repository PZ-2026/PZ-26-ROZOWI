package pl.edu.ur.blokur.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.PropertyRequest;
import pl.edu.ur.blokur.dto.PropertyResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Property;
import pl.edu.ur.blokur.repository.PropertyRepository;

/**
 * Serwis biznesowy obsługujący zarządzanie nieruchomościami (wspólnotami mieszkaniowymi). Zapewnia
 * operacje CRUD oraz obsługę przesyłania logo nieruchomości.
 */
@Service
public class PropertyService {

    /** Maksymalny dopuszczalny rozmiar pliku logo w bajtach (2 MB). */
    private static final long MAX_LOGO_SIZE_BYTES = 2L * 1024 * 1024;

    private final PropertyRepository propertyRepository;
    private final String uploadDir;

    /**
     * Konstruktor serwisu z wstrzyknięciem repozytorium i katalogu plików.
     *
     * @param propertyRepository repozytorium nieruchomości
     * @param uploadDir ścieżka katalogu przechowywania plików (konfigurowana przez {@code
     *     app.upload.dir})
     */
    public PropertyService(
            PropertyRepository propertyRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.propertyRepository = propertyRepository;
        this.uploadDir = uploadDir;
    }

    /**
     * Tworzy nową nieruchomość.
     *
     * @param request dane nowej nieruchomości
     * @return DTO z utworzoną nieruchomością
     * @throws BusinessValidationException jeśli podany NIP jest już zajęty przez inną nieruchomość
     */
    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        if (propertyRepository.existsByNip(request.getNip())) {
            throw new BusinessValidationException(
                    "Nieruchomość z NIP '" + request.getNip() + "' już istnieje");
        }

        Property property = new Property();
        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setNip(request.getNip());
        property.setManagerPhone(request.getManagerPhone());
        property.setManagerEmail(request.getManagerEmail());

        return toResponse(propertyRepository.save(property));
    }

    /**
     * Aktualizuje dane istniejącej nieruchomości. Logo nie jest modyfikowane tą metodą — do zmiany
     * logo służy {@link #uploadLogo(UUID, MultipartFile)}.
     *
     * @param id identyfikator nieruchomości do aktualizacji
     * @param request nowe dane nieruchomości
     * @return DTO z zaktualizowaną nieruchomością
     * @throws NotFoundException jeśli nieruchomość nie istnieje
     * @throws BusinessValidationException jeśli podany NIP jest zajęty przez inną nieruchomość
     */
    @Transactional
    public PropertyResponse update(UUID id, PropertyRequest request) {
        var property =
                propertyRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Nieruchomość o ID " + id + " nie istnieje"));

        if (propertyRepository.existsByNipAndIdNot(request.getNip(), id)) {
            throw new BusinessValidationException(
                    "Nieruchomość z NIP '" + request.getNip() + "' już istnieje");
        }

        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setNip(request.getNip());
        property.setManagerPhone(request.getManagerPhone());
        property.setManagerEmail(request.getManagerEmail());

        return toResponse(propertyRepository.save(property));
    }

    /**
     * Zwraca listę wszystkich nieruchomości.
     *
     * @return lista nieruchomości
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAll() {
        return propertyRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Zwraca dane wskazanej nieruchomości.
     *
     * @param id identyfikator nieruchomości
     * @return DTO nieruchomości
     * @throws NotFoundException jeśli nieruchomość nie istnieje
     */
    @Transactional(readOnly = true)
    public PropertyResponse getById(UUID id) {
        var property =
                propertyRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Nieruchomość o ID " + id + " nie istnieje"));
        return toResponse(property);
    }

    /**
     * Zapisuje logo nieruchomości na dysku i aktualizuje pole {@code logo_path}. Akceptowane
     * formaty: PNG, JPEG. Maksymalny rozmiar: 2 MB.
     *
     * @param id identyfikator nieruchomości
     * @param file przesłany plik graficzny
     * @return DTO nieruchomości z zaktualizowaną ścieżką logo
     * @throws NotFoundException jeśli nieruchomość nie istnieje
     * @throws BusinessValidationException jeśli typ pliku jest niedozwolony lub plik przekracza 2
     *     MB
     * @throws RuntimeException jeśli zapis pliku na dysk się nie powiedzie
     */
    @Transactional
    public PropertyResponse uploadLogo(UUID id, MultipartFile file) {
        var property =
                propertyRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Nieruchomość o ID " + id + " nie istnieje"));

        var contentType = file.getContentType();
        if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType)) {
            throw new BusinessValidationException(
                    "Logo musi być plikiem PNG lub JPEG (przesłany typ: " + contentType + ")");
        }

        if (file.getSize() > MAX_LOGO_SIZE_BYTES) {
            throw new BusinessValidationException("Logo nie może przekraczać 2 MB");
        }

        var extension = "image/png".equals(contentType) ? "png" : "jpg";
        var logoDir = Paths.get(uploadDir, "logos");

        try {
            Files.createDirectories(logoDir);
            var logoPath = logoDir.resolve(id + "." + extension);
            Files.copy(file.getInputStream(), logoPath, StandardCopyOption.REPLACE_EXISTING);
            property.setLogoPath(logoPath.toString());
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się zapisać pliku logo: " + e.getMessage(), e);
        }

        return toResponse(propertyRepository.save(property));
    }

    private PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getNip(),
                property.getManagerPhone(),
                property.getManagerEmail(),
                property.getLogoPath());
    }
}

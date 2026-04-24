package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pl.edu.ur.blokur.dto.PropertyRequest;
import pl.edu.ur.blokur.dto.PropertyResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Property;
import pl.edu.ur.blokur.repository.PropertyRepository;

/**
 * Testy jednostkowe dla {@link PropertyService}. Weryfikują logikę biznesową zarządzania
 * nieruchomościami: tworzenie, aktualizację, pobieranie, przesyłanie logo oraz walidację NIP.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyService — zarządzanie nieruchomościami")
class PropertyServiceTest {

    @TempDir Path tempDir;

    @Mock private PropertyRepository propertyRepository;

    private PropertyService propertyService;

    private UUID propertyId;
    private Property property;
    private PropertyRequest validRequest;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(propertyRepository, tempDir.toString());

        propertyId = UUID.randomUUID();

        property = new Property();
        property.setId(propertyId);
        property.setName("Wspólnota Słoneczna");
        property.setAddress("ul. Kwiatowa 1, 35-000 Rzeszów");
        property.setNip("1234567890");
        property.setManagerPhone("123456789");
        property.setManagerEmail("zarzadca@blokur.pl");
        property.setLogoPath(null);

        validRequest =
                new PropertyRequest(
                        "Wspólnota Słoneczna",
                        "ul. Kwiatowa 1, 35-000 Rzeszów",
                        "1234567890",
                        "123456789",
                        "zarzadca@blokur.pl");
    }

    // =======================================================
    // CREATE
    // =======================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zapisuje nieruchomość i zwraca DTO")
        void shouldCreatePropertySuccessfully() {
            when(propertyRepository.existsByNip("1234567890")).thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(
                            inv -> {
                                Property p = inv.getArgument(0);
                                p.setId(propertyId);
                                return p;
                            });

            PropertyResponse response = propertyService.create(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(propertyId);
            assertThat(response.getName()).isEqualTo("Wspólnota Słoneczna");
            assertThat(response.getAddress()).isEqualTo("ul. Kwiatowa 1, 35-000 Rzeszów");
            assertThat(response.getNip()).isEqualTo("1234567890");
            assertThat(response.getManagerPhone()).isEqualTo("123456789");
            assertThat(response.getManagerEmail()).isEqualTo("zarzadca@blokur.pl");
            assertThat(response.getLogoPath()).isNull();
        }

        @Test
        @DisplayName("Nowa nieruchomość ma logo_path = null")
        void shouldCreatePropertyWithNullLogoPath() {
            when(propertyRepository.existsByNip("1234567890")).thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(
                            inv -> {
                                Property p = inv.getArgument(0);
                                p.setId(propertyId);
                                return p;
                            });

            PropertyResponse response = propertyService.create(validRequest);

            assertThat(response.getLogoPath()).isNull();
        }

        @Test
        @DisplayName("Zapisuje wszystkie pola z requestu do encji")
        void shouldPassAllFieldsToEntity() {
            when(propertyRepository.existsByNip("1234567890")).thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            propertyService.create(validRequest);

            ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
            verify(propertyRepository).save(captor.capture());
            Property saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("Wspólnota Słoneczna");
            assertThat(saved.getAddress()).isEqualTo("ul. Kwiatowa 1, 35-000 Rzeszów");
            assertThat(saved.getNip()).isEqualTo("1234567890");
            assertThat(saved.getManagerPhone()).isEqualTo("123456789");
            assertThat(saved.getManagerEmail()).isEqualTo("zarzadca@blokur.pl");
        }

        @Test
        @DisplayName("NIP już zajęty — rzuca BusinessValidationException")
        void shouldThrowWhenNipAlreadyExists() {
            when(propertyRepository.existsByNip("1234567890")).thenReturn(true);

            assertThatThrownBy(() -> propertyService.create(validRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("1234567890");

            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Pola opcjonalne (phone, email) mogą być null")
        void shouldAllowNullOptionalFields() {
            PropertyRequest noContactRequest =
                    new PropertyRequest("Wspólnota B", "ul. Nowa 2", "0987654321", null, null);
            when(propertyRepository.existsByNip("0987654321")).thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(
                            inv -> {
                                Property p = inv.getArgument(0);
                                p.setId(UUID.randomUUID());
                                return p;
                            });

            PropertyResponse response = propertyService.create(noContactRequest);

            assertThat(response.getManagerPhone()).isNull();
            assertThat(response.getManagerEmail()).isNull();
        }
    }

    // =======================================================
    // UPDATE
    // =======================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Poprawne dane — aktualizuje nieruchomość i zwraca DTO")
        void shouldUpdatePropertySuccessfully() {
            PropertyRequest updateRequest =
                    new PropertyRequest(
                            "Nowa Nazwa",
                            "ul. Nowa 99, 35-001 Rzeszów",
                            "1234567890",
                            "987654321",
                            "nowy@blokur.pl");
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.existsByNipAndIdNot("1234567890", propertyId))
                    .thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = propertyService.update(propertyId, updateRequest);

            assertThat(response.getName()).isEqualTo("Nowa Nazwa");
            assertThat(response.getAddress()).isEqualTo("ul. Nowa 99, 35-001 Rzeszów");
            assertThat(response.getNip()).isEqualTo("1234567890");
            assertThat(response.getManagerPhone()).isEqualTo("987654321");
            assertThat(response.getManagerEmail()).isEqualTo("nowy@blokur.pl");
        }

        @Test
        @DisplayName("Logo nie jest usuwane przy aktualizacji danych")
        void shouldNotClearLogoDuringUpdate() {
            property.setLogoPath("/uploads/logos/some.png");
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.existsByNipAndIdNot("1234567890", propertyId))
                    .thenReturn(false);
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = propertyService.update(propertyId, validRequest);

            assertThat(response.getLogoPath()).isEqualTo("/uploads/logos/some.png");
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — rzuca NotFoundException")
        void shouldThrowNotFoundWhenPropertyDoesNotExist() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propertyService.update(propertyId, validRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(propertyId.toString());

            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("NIP zajęty przez inną nieruchomość — rzuca BusinessValidationException")
        void shouldThrowWhenNipTakenByOtherProperty() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.existsByNipAndIdNot("1234567890", propertyId)).thenReturn(true);

            assertThatThrownBy(() -> propertyService.update(propertyId, validRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("1234567890");

            verify(propertyRepository, never()).save(any());
        }
    }

    // =======================================================
    // GET ALL
    // =======================================================

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Zwraca listę wszystkich nieruchomości")
        void shouldReturnAllProperties() {
            Property second = new Property();
            second.setId(UUID.randomUUID());
            second.setName("Wspólnota B");
            second.setAddress("ul. Wiśniowa 5");
            second.setNip("0987654321");

            when(propertyRepository.findAll()).thenReturn(List.of(property, second));

            List<PropertyResponse> result = propertyService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(PropertyResponse::getNip)
                    .containsExactlyInAnyOrder("1234567890", "0987654321");
        }

        @Test
        @DisplayName("Brak nieruchomości — zwraca pustą listę")
        void shouldReturnEmptyListWhenNoProperties() {
            when(propertyRepository.findAll()).thenReturn(List.of());

            List<PropertyResponse> result = propertyService.getAll();

            assertThat(result).isEmpty();
        }
    }

    // =======================================================
    // GET BY ID
    // =======================================================

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Istniejąca nieruchomość — zwraca DTO")
        void shouldReturnPropertyById() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            PropertyResponse response = propertyService.getById(propertyId);

            assertThat(response.getId()).isEqualTo(propertyId);
            assertThat(response.getNip()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentProperty() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propertyService.getById(propertyId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(propertyId.toString());
        }
    }

    // =======================================================
    // UPLOAD LOGO
    // =======================================================

    @Nested
    @DisplayName("uploadLogo()")
    class UploadLogoTests {

        @Test
        @DisplayName("Plik PNG — zapisuje na dysku i aktualizuje logo_path")
        void shouldSavePngLogoAndUpdatePath() throws IOException {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "logo.png",
                            "image/png",
                            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = propertyService.uploadLogo(propertyId, file);

            assertThat(response.getLogoPath()).isNotNull();
            assertThat(response.getLogoPath()).endsWith(propertyId + ".png");
            assertThat(Files.exists(Path.of(response.getLogoPath()))).isTrue();
        }

        @Test
        @DisplayName("Plik JPEG — zapisuje na dysku i aktualizuje logo_path")
        void shouldSaveJpegLogoAndUpdatePath() throws IOException {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "logo.jpg",
                            "image/jpeg",
                            new byte[] {(byte) 0xFF, (byte) 0xD8});
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = propertyService.uploadLogo(propertyId, file);

            assertThat(response.getLogoPath()).endsWith(propertyId + ".jpg");
        }

        @Test
        @DisplayName("Niedozwolony typ pliku — rzuca BusinessValidationException")
        void shouldThrowForUnsupportedContentType() {
            MockMultipartFile file =
                    new MockMultipartFile("file", "logo.gif", "image/gif", new byte[] {0x01});
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> propertyService.uploadLogo(propertyId, file))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("PNG lub JPEG");

            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Plik przekracza 2 MB — rzuca BusinessValidationException")
        void shouldThrowWhenFileTooLarge() {
            byte[] bigContent = new byte[2 * 1024 * 1024 + 1];
            MockMultipartFile file =
                    new MockMultipartFile("file", "big.png", "image/png", bigContent);
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> propertyService.uploadLogo(propertyId, file))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("2 MB");

            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejąca nieruchomość — rzuca NotFoundException")
        void shouldThrowNotFoundWhenPropertyDoesNotExist() {
            MockMultipartFile file =
                    new MockMultipartFile("file", "logo.png", "image/png", new byte[] {0x01});
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propertyService.uploadLogo(propertyId, file))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(propertyId.toString());

            verify(propertyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Przesłanie nowego logo nadpisuje poprzedni plik")
        void shouldReplaceExistingLogo() throws IOException {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "new.png", "image/png", new byte[] {(byte) 0x89, 0x50});
            property.setLogoPath(tempDir.resolve("logos").resolve(propertyId + ".png").toString());
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(any(Property.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PropertyResponse response = propertyService.uploadLogo(propertyId, file);

            assertThat(response.getLogoPath()).endsWith(propertyId + ".png");
        }
    }
}

package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.models.Ticket;
import pl.edu.ur.blokur.models.TicketImage;
import pl.edu.ur.blokur.models.TicketImageType;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.TicketImageRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TicketImageServiceTest {

    @Mock private TicketImageRepository ticketImageRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;

    private TicketImageService ticketImageService;

    private User resident;
    private User conservator;
    private Ticket ticket;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        ticketImageService =
                new TicketImageService(
                        ticketImageRepository,
                        ticketRepository,
                        userRepository,
                        System.getProperty("java.io.tmpdir"));

        resident = new User();
        resident.setId(UUID.randomUUID());
        resident.setRole("MIESZKANIEC");
        resident.setEmail("mieszkaniec@test.com");

        pl.edu.ur.blokur.models.Apartment apartment = new pl.edu.ur.blokur.models.Apartment();
        apartment.setId(UUID.randomUUID());

        pl.edu.ur.blokur.models.UserApartment ua = new pl.edu.ur.blokur.models.UserApartment();
        ua.setUser(resident);
        ua.setApartment(apartment);
        resident.getUserApartments().add(ua);

        conservator = new User();
        conservator.setId(UUID.randomUUID());
        conservator.setRole("KONSERWATOR");
        conservator.setEmail("konserwator@test.com");

        ticketId = UUID.randomUUID();
        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setAuthor(resident);
        ticket.setAssignedTo(conservator);
        ticket.setApartment(apartment);
    }

    @Nested
    @DisplayName("Walidacja wgrywania zdjęć")
    class UploadValidationTests {

        @Test
        @DisplayName("Zły format MIME - rzuca BusinessValidationException")
        void shouldThrowExceptionWhenWrongMimeType() {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "test.pdf", "application/pdf", "dummy data".getBytes());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(resident.getEmail())).thenReturn(Optional.of(resident));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.BEFORE,
                                            resident.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Niedozwolony format pliku");
        }

        @Test
        @DisplayName("Pusty plik - rzuca BusinessValidationException")
        void shouldThrowExceptionWhenEmptyFile() {
            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(resident.getEmail())).thenReturn(Optional.of(resident));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.BEFORE,
                                            resident.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Plik jest pusty");
        }

        @Test
        @DisplayName("Limit plików BEFORE (max 5) jest egzekwowany")
        void shouldEnforceLimitOnBeforeImages() {
            for (int i = 0; i < 5; i++) {
                TicketImage img = new TicketImage();
                img.setImageType(TicketImageType.BEFORE);
                ticket.getImages().add(img);
            }

            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(resident.getEmail())).thenReturn(Optional.of(resident));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.BEFORE,
                                            resident.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Osiągnięto limit 5 zdjęć przed naprawą");
        }
    }

    @Nested
    @DisplayName("Uprawnienia do wgrywania")
    class UploadAccessTests {

        @Test
        @DisplayName("Obcy mieszkaniec nie może wgrać zdjęcia BEFORE")
        void shouldDenyForeignResidentBeforeUpload() {
            User foreignResident = new User();
            foreignResident.setId(UUID.randomUUID());
            foreignResident.setRole("MIESZKANIEC");
            foreignResident.setEmail("obcy@test.com");

            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(foreignResident.getEmail()))
                    .thenReturn(Optional.of(foreignResident));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.BEFORE,
                                            foreignResident.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Brak dostępu do zgłoszenia");
        }

        @Test
        @DisplayName("Obcy konserwator nie może wgrać zdjęcia AFTER")
        void shouldDenyForeignConservatorAfterUpload() {
            User foreignConservator = new User();
            foreignConservator.setId(UUID.randomUUID());
            foreignConservator.setRole("KONSERWATOR");
            foreignConservator.setEmail("obcy_k@test.com");

            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(foreignConservator.getEmail()))
                    .thenReturn(Optional.of(foreignConservator));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.AFTER,
                                            foreignConservator.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Zgłoszenie nie jest przypisane do tego konserwatora");
        }

        @Test
        @DisplayName("Konserwator bez przypisania nie może wgrać zdjęcia AFTER (ticket.assignedTo null)")
        void shouldDenyConservatorWhenTicketHasNoAssignment() {
            ticket.setAssignedTo(null);

            MockMultipartFile file =
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(conservator.getEmail()))
                    .thenReturn(Optional.of(conservator));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.uploadImage(
                                            ticketId,
                                            file,
                                            TicketImageType.AFTER,
                                            conservator.getEmail()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("Zgłoszenie nie jest przypisane do tego konserwatora");
        }
    }

    @Nested
    @DisplayName("getImagesForTicket — pobieranie listy zdjęć")
    class GetImagesForTicketTests {

        @Test
        @DisplayName("Zarządca widzi zdjęcia wszystkich zgłoszeń")
        void zarzadcaCanViewImages() {
            User zarzadca = new User();
            zarzadca.setId(UUID.randomUUID());
            zarzadca.setRole("ZARZADCA");
            zarzadca.setEmail("zarzadca@test.com");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(zarzadca.getEmail())).thenReturn(Optional.of(zarzadca));
            when(ticketImageRepository.findByTicketIdOrderByUploadedAtAsc(ticketId))
                    .thenReturn(java.util.Collections.emptyList());

            var result = ticketImageService.getImagesForTicket(ticketId, zarzadca.getEmail());

            org.assertj.core.api.Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Mieszkaniec może przeglądać zdjęcia własnego zgłoszenia")
        void residentCanViewOwnTicketImages() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(resident.getEmail())).thenReturn(Optional.of(resident));
            when(ticketImageRepository.findByTicketIdOrderByUploadedAtAsc(ticketId))
                    .thenReturn(java.util.Collections.emptyList());

            var result = ticketImageService.getImagesForTicket(ticketId, resident.getEmail());

            org.assertj.core.api.Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Obcy mieszkaniec nie może przeglądać cudzych zdjęć")
        void foreignResidentCannotViewImages() {
            User foreignResident = new User();
            foreignResident.setId(UUID.randomUUID());
            foreignResident.setRole("MIESZKANIEC");
            foreignResident.setEmail("obcy2@test.com");

            pl.edu.ur.blokur.models.Apartment otherApartment = new pl.edu.ur.blokur.models.Apartment();
            otherApartment.setId(UUID.randomUUID());

            pl.edu.ur.blokur.models.UserApartment ua = new pl.edu.ur.blokur.models.UserApartment();
            ua.setUser(foreignResident);
            ua.setApartment(otherApartment);
            foreignResident.getUserApartments().add(ua);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(foreignResident.getEmail()))
                    .thenReturn(Optional.of(foreignResident));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.getImagesForTicket(
                                             ticketId, foreignResident.getEmail()))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Konserwator przypisany może przeglądać zdjęcia")
        void assignedConservatorCanViewImages() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(conservator.getEmail()))
                    .thenReturn(Optional.of(conservator));
            when(ticketImageRepository.findByTicketIdOrderByUploadedAtAsc(ticketId))
                    .thenReturn(java.util.Collections.emptyList());

            var result = ticketImageService.getImagesForTicket(ticketId, conservator.getEmail());

            org.assertj.core.api.Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Nieprzypisany konserwator nie może przeglądać zdjęć")
        void unassignedConservatorCannotViewImages() {
            User otherConservator = new User();
            otherConservator.setId(UUID.randomUUID());
            otherConservator.setRole("KONSERWATOR");
            otherConservator.setEmail("inny_k@test.com");

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(userRepository.findByEmail(otherConservator.getEmail()))
                    .thenReturn(Optional.of(otherConservator));

            assertThatThrownBy(
                            () ->
                                    ticketImageService.getImagesForTicket(
                                            ticketId, otherConservator.getEmail()))
                    .isInstanceOf(SecurityException.class);
        }
    }
}

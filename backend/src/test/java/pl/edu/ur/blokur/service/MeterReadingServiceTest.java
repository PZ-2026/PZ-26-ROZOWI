package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.edu.ur.blokur.dto.MeterReadingRequest;
import pl.edu.ur.blokur.dto.MeterReadingResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.MediumType;
import pl.edu.ur.blokur.models.Meter;
import pl.edu.ur.blokur.models.MeterReading;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.MeterReadingRepository;
import pl.edu.ur.blokur.repository.MeterRepository;

/**
<<<<<<< HEAD
 * Testy jednostkowe dla {@link MeterReadingService}.
 * Weryfikują logikę biznesową odczytów liczników: tworzenie, pobieranie,
 * aktualizację, usuwanie oraz walidację duplikatów i regresji wartości
 * po przejściu na referencję do encji {@link Meter}.
=======
 * Testy jednostkowe dla {@link MeterReadingService}. Weryfikują logikę biznesową odczytów
 * liczników: tworzenie, pobieranie, aktualizację, usuwanie oraz walidację duplikatów i regresji
 * wartości.
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeterReadingService — serwis odczytów liczników")
class MeterReadingServiceTest {

    @Mock private MeterReadingRepository meterReadingRepository;

    @Mock private ApartmentRepository apartmentRepository;

<<<<<<< HEAD
    @Mock
    private MeterRepository meterRepository;

    @InjectMocks
    private MeterReadingService meterReadingService;
=======
    @InjectMocks private MeterReadingService meterReadingService;
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)

    private UUID apartmentId;
    private UUID meterId;
    private UUID readingId;
    private Apartment apartment;
    private Meter meter;
    private MeterReading existingReading;
    private MeterReadingRequest validRequest;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();
<<<<<<< HEAD
        meterId = UUID.randomUUID();
=======
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
        readingId = UUID.randomUUID();

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("1");

        meter = new Meter();
        meter.setId(meterId);
        meter.setApartment(apartment);
        meter.setSerialNumber("SN-100");
        meter.setMediumType(MediumType.CIEPLA_WODA);
        meter.setInstallationDate(LocalDate.of(2025, 1, 1));
        meter.setActive(true);

        existingReading = new MeterReading();
        existingReading.setId(readingId);
        existingReading.setApartment(apartment);
        existingReading.setMeter(meter);
        existingReading.setValue(new BigDecimal("100.0000"));
        existingReading.setReadingDate(LocalDate.of(2026, 3, 1));
        existingReading.setCreatedAt(LocalDateTime.now());
        existingReading.setUpdatedAt(LocalDateTime.now());

<<<<<<< HEAD
        validRequest = new MeterReadingRequest(
            meterId,
            new BigDecimal("150.0000"),
            LocalDate.of(2026, 4, 1)
        );
=======
        validRequest =
                new MeterReadingRequest(
                        "CIEPLA_WODA", new BigDecimal("150.0000"), LocalDate.of(2026, 4, 1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
    }

    // =======================================================
    // CREATE
    // =======================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca DTO z zapisanym odczytem")
        void shouldCreateReadingSuccessfully() {
            MeterReading saved = new MeterReading();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setMeter(meter);
            saved.setValue(validRequest.getValue());
            saved.setReadingDate(validRequest.getReadingDate());
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
<<<<<<< HEAD
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(
                eq(meterId), eq(LocalDate.of(2026, 4, 1))
            )).thenReturn(false);
            when(meterReadingRepository
                .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(eq(meterId)))
                .thenReturn(null);
=======
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndDeletedFalse(
                                    eq(apartmentId),
                                    eq("CIEPLA_WODA"),
                                    eq(LocalDate.of(2026, 4, 1))))
                    .thenReturn(false);
            when(meterReadingRepository
                            .findTopByApartmentIdAndMeterTypeAndDeletedFalseOrderByReadingDateDesc(
                                    eq(apartmentId), eq("CIEPLA_WODA")))
                    .thenReturn(null);
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(meterReadingRepository.save(any(MeterReading.class))).thenReturn(saved);

            MeterReadingResponse response = meterReadingService.create(apartmentId, validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getMeterId()).isEqualTo(meterId);
            assertThat(response.getMediumType()).isEqualTo(MediumType.CIEPLA_WODA);
            assertThat(response.getMeterSerialNumber()).isEqualTo("SN-100");
            assertThat(response.getValue()).isEqualByComparingTo("150.0000");
            assertThat(response.getApartmentId()).isEqualTo(apartmentId);
            verify(meterReadingRepository).save(any(MeterReading.class));
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundWhenApartmentDoesNotExist() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący licznik — rzuca NotFoundException")
        void shouldThrowNotFoundWhenMeterDoesNotExist() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.findById(meterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(meterId.toString());

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Licznik należy do innego lokalu — rzuca BusinessValidationException")
        void shouldThrowWhenMeterBelongsToAnotherApartment() {
            Apartment otherApartment = new Apartment();
            otherApartment.setId(UUID.randomUUID());
            meter.setApartment(otherApartment);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("nie jest przypisany");

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieaktywny licznik — rzuca BusinessValidationException")
        void shouldThrowWhenMeterIsInactive() {
            meter.setActive(false);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("nieaktywny");

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Duplikat (ten sam licznik i data) — rzuca BusinessValidationException")
        void shouldThrowWhenDuplicateReadingOnCreate() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
<<<<<<< HEAD
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(
                eq(meterId), eq(LocalDate.of(2026, 4, 1))
            )).thenReturn(true);

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining(meterId.toString());
=======
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndDeletedFalse(
                                    eq(apartmentId),
                                    eq("CIEPLA_WODA"),
                                    eq(LocalDate.of(2026, 4, 1))))
                    .thenReturn(true);

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, validRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("CIEPLA_WODA");
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wartość niższa niż ostatni odczyt — rzuca BusinessValidationException")
        void shouldThrowWhenNewValueIsLowerThanLastReading() {
<<<<<<< HEAD
            MeterReadingRequest regression = new MeterReadingRequest(
                meterId,
                new BigDecimal("50.0000"),
                LocalDate.of(2026, 4, 1)
            );

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(any(), any()))
                .thenReturn(false);
            when(meterReadingRepository
                .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(eq(meterId)))
                .thenReturn(existingReading);
=======
            MeterReadingRequest regression =
                    new MeterReadingRequest(
                            "CIEPLA_WODA",
                            new BigDecimal("50.0000"), // mniejsza niż 100 w existingReading
                            LocalDate.of(2026, 4, 1));

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndDeletedFalse(
                                    any(), any(), any()))
                    .thenReturn(false);
            when(meterReadingRepository
                            .findTopByApartmentIdAndMeterTypeAndDeletedFalseOrderByReadingDateDesc(
                                    eq(apartmentId), eq("CIEPLA_WODA")))
                    .thenReturn(existingReading);
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)

            assertThatThrownBy(() -> meterReadingService.create(apartmentId, regression))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("50");

            verify(meterReadingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Brak poprzednich odczytów — regresja nie jest sprawdzana")
        void shouldNotCheckRegressionWhenNoLatestReading() {
            MeterReading saved = new MeterReading();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setMeter(meter);
            saved.setValue(validRequest.getValue());
            saved.setReadingDate(validRequest.getReadingDate());
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
<<<<<<< HEAD
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(any(), any()))
                .thenReturn(false);
            when(meterReadingRepository
                .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(any()))
                .thenReturn(null);
=======
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndDeletedFalse(
                                    any(), any(), any()))
                    .thenReturn(false);
            when(meterReadingRepository
                            .findTopByApartmentIdAndMeterTypeAndDeletedFalseOrderByReadingDateDesc(
                                    any(), any()))
                    .thenReturn(null);
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(meterReadingRepository.save(any())).thenReturn(saved);

            MeterReadingResponse response = meterReadingService.create(apartmentId, validRequest);

            assertThat(response).isNotNull();
        }
    }

    // =======================================================
    // GET BY ID
    // =======================================================

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Istniejący odczyt — zwraca poprawne DTO")
        void shouldReturnReadingById() {
            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.of(existingReading));

            MeterReadingResponse response = meterReadingService.getById(readingId);

            assertThat(response.getId()).isEqualTo(readingId);
            assertThat(response.getMeterId()).isEqualTo(meterId);
            assertThat(response.getMediumType()).isEqualTo(MediumType.CIEPLA_WODA);
        }

        @Test
        @DisplayName("Nieistniejący lub usunięty odczyt — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentReading() {
            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterReadingService.getById(readingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(readingId.toString());
        }
    }

    // =======================================================
    // GET ALL BY APARTMENT
    // =======================================================

    @Nested
    @DisplayName("getAllByApartment()")
    class GetAllByApartmentTests {

        @Test
        @DisplayName("Istniejący lokal — zwraca stronicowaną listę odczytów")
        void shouldReturnPagedReadings() {
            Page<MeterReading> page = new PageImpl<>(List.of(existingReading));
            when(apartmentRepository.existsById(apartmentId)).thenReturn(true);
            when(meterReadingRepository.findByApartmentIdAndDeletedFalse(
                            eq(apartmentId), any(Pageable.class)))
                    .thenReturn(page);

            Page<MeterReadingResponse> result =
                    meterReadingService.getAllByApartment(apartmentId, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getMediumType()).isEqualTo(MediumType.CIEPLA_WODA);
            assertThat(result.getContent().get(0).getMeterId()).isEqualTo(meterId);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentApartment() {
            when(apartmentRepository.existsById(apartmentId)).thenReturn(false);

            assertThatThrownBy(() -> meterReadingService.getAllByApartment(apartmentId, 0, 10))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());
        }
    }

    // =======================================================
    // UPDATE
    // =======================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Poprawne dane — zwraca zaktualizowane DTO")
        void shouldUpdateReadingSuccessfully() {
<<<<<<< HEAD
            MeterReadingRequest updateRequest = new MeterReadingRequest(
                meterId,
                new BigDecimal("200.0000"),
                LocalDate.of(2026, 5, 1)
            );
=======
            MeterReadingRequest updateRequest =
                    new MeterReadingRequest(
                            "CIEPLA_WODA", new BigDecimal("200.0000"), LocalDate.of(2026, 5, 1));
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)

            MeterReading updated = new MeterReading();
            updated.setId(readingId);
            updated.setApartment(apartment);
            updated.setMeter(meter);
            updated.setValue(new BigDecimal("200.0000"));
            updated.setReadingDate(LocalDate.of(2026, 5, 1));
            updated.setCreatedAt(LocalDateTime.now());
            updated.setUpdatedAt(LocalDateTime.now());

            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
<<<<<<< HEAD
                .thenReturn(Optional.of(existingReading));
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository
                .existsByMeterIdAndReadingDateAndIdNotAndDeletedFalse(
                    eq(meterId), eq(LocalDate.of(2026, 5, 1)), eq(readingId)
                )).thenReturn(false);
            when(meterReadingRepository
                .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(eq(meterId)))
                .thenReturn(existingReading);
=======
                    .thenReturn(Optional.of(existingReading));
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndIdNotAndDeletedFalse(
                                    eq(apartmentId),
                                    eq("CIEPLA_WODA"),
                                    eq(LocalDate.of(2026, 5, 1)),
                                    eq(readingId)))
                    .thenReturn(false);
            when(meterReadingRepository
                            .findTopByApartmentIdAndMeterTypeAndDeletedFalseOrderByReadingDateDesc(
                                    eq(apartmentId), eq("CIEPLA_WODA")))
                    .thenReturn(existingReading);
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)
            when(meterReadingRepository.save(any())).thenReturn(updated);

            MeterReadingResponse response = meterReadingService.update(readingId, updateRequest);

            assertThat(response.getValue()).isEqualByComparingTo("200.0000");
        }

        @Test
        @DisplayName("Nieistniejący odczyt — rzuca NotFoundException")
        void shouldThrowNotFoundWhenReadingDoesNotExist() {
            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterReadingService.update(readingId, validRequest))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Duplikat podczas aktualizacji — rzuca BusinessValidationException")
        void shouldThrowWhenDuplicateOnUpdate() {
<<<<<<< HEAD
            MeterReadingRequest updateRequest = new MeterReadingRequest(
                meterId,
                new BigDecimal("200.0000"),
                LocalDate.of(2026, 4, 1)
            );

            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                .thenReturn(Optional.of(existingReading));
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterReadingRepository
                .existsByMeterIdAndReadingDateAndIdNotAndDeletedFalse(any(), any(), any()))
                .thenReturn(true);
=======
            MeterReadingRequest updateRequest =
                    new MeterReadingRequest(
                            "CIEPLA_WODA", new BigDecimal("200.0000"), LocalDate.of(2026, 4, 1));

            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.of(existingReading));
            when(meterReadingRepository
                            .existsByApartmentIdAndMeterTypeAndReadingDateAndIdNotAndDeletedFalse(
                                    any(), any(), any(), any()))
                    .thenReturn(true);
>>>>>>> ffc02e6 (uzupełnienie Javadoc w modelach, DTO i serwisach backendu)

            assertThatThrownBy(() -> meterReadingService.update(readingId, updateRequest))
                    .isInstanceOf(BusinessValidationException.class);
        }
    }

    // =======================================================
    // DELETE
    // =======================================================

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Istniejący odczyt — ustawia flagę deleted i zapisuje")
        void shouldSoftDeleteReading() {
            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.of(existingReading));

            meterReadingService.delete(readingId);

            assertThat(existingReading.isDeleted()).isTrue();
            verify(meterReadingRepository).save(existingReading);
        }

        @Test
        @DisplayName("Nieistniejący odczyt — rzuca NotFoundException")
        void shouldThrowNotFoundWhenDeletingNonExistentReading() {
            when(meterReadingRepository.findByIdAndDeletedFalse(readingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterReadingService.delete(readingId))
                    .isInstanceOf(NotFoundException.class);

            verify(meterReadingRepository, never()).save(any());
        }
    }
}

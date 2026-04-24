package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.MeterRequest;
import pl.edu.ur.blokur.dto.MeterResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.MediumType;
import pl.edu.ur.blokur.models.Meter;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.MeterRepository;

/**
 * Testy jednostkowe dla {@link MeterService}. Weryfikują logikę biznesową zarządzania licznikami:
 * dodawanie do lokalu, pobieranie listy, dezaktywacja oraz walidację.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MeterService — zarządzanie licznikami lokalu")
class MeterServiceTest {

    @Mock private MeterRepository meterRepository;

    @Mock private ApartmentRepository apartmentRepository;

    @InjectMocks private MeterService meterService;

    private UUID apartmentId;
    private UUID meterId;
    private Apartment apartment;
    private Meter meter;
    private MeterRequest validRequest;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();
        meterId = UUID.randomUUID();

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("12");

        meter = new Meter();
        meter.setId(meterId);
        meter.setApartment(apartment);
        meter.setSerialNumber("SN-12345");
        meter.setMediumType(MediumType.ZIMNA_WODA);
        meter.setInstallationDate(LocalDate.of(2025, 6, 15));
        meter.setActive(true);

        validRequest =
                new MeterRequest("SN-12345", MediumType.ZIMNA_WODA, LocalDate.of(2025, 6, 15));
    }

    // =======================================================
    // CREATE
    // =======================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Poprawne dane — zapisuje licznik i zwraca DTO")
        void shouldCreateMeterSuccessfully() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.existsBySerialNumber("SN-12345")).thenReturn(false);
            when(meterRepository.save(any(Meter.class)))
                    .thenAnswer(
                            inv -> {
                                Meter m = inv.getArgument(0);
                                m.setId(meterId);
                                return m;
                            });

            MeterResponse response = meterService.create(apartmentId, validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(meterId);
            assertThat(response.getApartmentId()).isEqualTo(apartmentId);
            assertThat(response.getSerialNumber()).isEqualTo("SN-12345");
            assertThat(response.getMediumType()).isEqualTo(MediumType.ZIMNA_WODA);
            assertThat(response.getInstallationDate()).isEqualTo(LocalDate.of(2025, 6, 15));
            assertThat(response.isActive()).isTrue();
        }

        @Test
        @DisplayName("Nowy licznik zawsze jest aktywny (is_active=true)")
        void shouldSetNewMeterAsActive() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.existsBySerialNumber("SN-12345")).thenReturn(false);
            when(meterRepository.save(any(Meter.class))).thenAnswer(inv -> inv.getArgument(0));

            meterService.create(apartmentId, validRequest);

            ArgumentCaptor<Meter> captor = ArgumentCaptor.forClass(Meter.class);
            verify(meterRepository).save(captor.capture());
            assertThat(captor.getValue().isActive()).isTrue();
            assertThat(captor.getValue().getApartment()).isEqualTo(apartment);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundWhenApartmentDoesNotExist() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterService.create(apartmentId, validRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());

            verify(meterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Numer seryjny już istnieje — rzuca BusinessValidationException")
        void shouldThrowWhenSerialNumberDuplicate() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.existsBySerialNumber("SN-12345")).thenReturn(true);

            assertThatThrownBy(() -> meterService.create(apartmentId, validRequest))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("SN-12345");

            verify(meterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Zapisuje wszystkie wartości zgodnie z requestem")
        void shouldPassAllRequestValuesToEntity() {
            MeterRequest req =
                    new MeterRequest("SN-777", MediumType.GAZ, LocalDate.of(2024, 2, 10));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(meterRepository.existsBySerialNumber("SN-777")).thenReturn(false);
            when(meterRepository.save(any(Meter.class))).thenAnswer(inv -> inv.getArgument(0));

            MeterResponse response = meterService.create(apartmentId, req);

            assertThat(response.getSerialNumber()).isEqualTo("SN-777");
            assertThat(response.getMediumType()).isEqualTo(MediumType.GAZ);
            assertThat(response.getInstallationDate()).isEqualTo(LocalDate.of(2024, 2, 10));
        }
    }

    // =======================================================
    // GET ALL BY APARTMENT
    // =======================================================

    @Nested
    @DisplayName("getAllByApartment()")
    class GetAllByApartmentTests {

        @Test
        @DisplayName("Istniejący lokal — zwraca listę liczników")
        void shouldReturnMetersForApartment() {
            Meter inactive = new Meter();
            inactive.setId(UUID.randomUUID());
            inactive.setApartment(apartment);
            inactive.setSerialNumber("SN-OLD");
            inactive.setMediumType(MediumType.CIEPLO);
            inactive.setInstallationDate(LocalDate.of(2020, 1, 1));
            inactive.setActive(false);

            when(apartmentRepository.existsById(apartmentId)).thenReturn(true);
            when(meterRepository.findByApartmentId(apartmentId))
                    .thenReturn(List.of(meter, inactive));

            List<MeterResponse> result = meterService.getAllByApartment(apartmentId);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MeterResponse::getSerialNumber)
                    .containsExactlyInAnyOrder("SN-12345", "SN-OLD");
            assertThat(result)
                    .extracting(MeterResponse::isActive)
                    .containsExactlyInAnyOrder(true, false);
        }

        @Test
        @DisplayName("Brak liczników w lokalu — zwraca pustą listę")
        void shouldReturnEmptyListWhenNoMeters() {
            when(apartmentRepository.existsById(apartmentId)).thenReturn(true);
            when(meterRepository.findByApartmentId(apartmentId)).thenReturn(List.of());

            List<MeterResponse> result = meterService.getAllByApartment(apartmentId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentApartment() {
            when(apartmentRepository.existsById(apartmentId)).thenReturn(false);

            assertThatThrownBy(() -> meterService.getAllByApartment(apartmentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());

            verify(meterRepository, never()).findByApartmentId(any());
        }
    }

    // =======================================================
    // DEACTIVATE
    // =======================================================

    @Nested
    @DisplayName("deactivate()")
    class DeactivateTests {

        @Test
        @DisplayName("Aktywny licznik — ustawia is_active=false i zapisuje")
        void shouldDeactivateActiveMeter() {
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
            when(meterRepository.save(any(Meter.class))).thenAnswer(inv -> inv.getArgument(0));

            MeterResponse response = meterService.deactivate(meterId);

            assertThat(response.isActive()).isFalse();
            assertThat(meter.isActive()).isFalse();
            verify(meterRepository).save(meter);
        }

        @Test
        @DisplayName("Nieistniejący licznik — rzuca NotFoundException")
        void shouldThrowNotFoundWhenMeterDoesNotExist() {
            when(meterRepository.findById(meterId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> meterService.deactivate(meterId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(meterId.toString());

            verify(meterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Licznik już nieaktywny — rzuca BusinessValidationException")
        void shouldThrowWhenMeterAlreadyInactive() {
            meter.setActive(false);
            when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));

            assertThatThrownBy(() -> meterService.deactivate(meterId))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("nieaktywny");

            verify(meterRepository, never()).save(any());
        }
    }
}

package pl.edu.ur.blokur.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.dto.MeterReadingRequest;
import pl.edu.ur.blokur.dto.MeterReadingResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Meter;
import pl.edu.ur.blokur.models.MeterReading;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.MeterReadingRepository;
import pl.edu.ur.blokur.repository.MeterRepository;

import java.util.UUID;

/**
 * Serwis biznesowy obsługujący logikę odczytów liczników.
 * Zawiera walidację duplikatów, regresji wartości oraz mapowanie DTO.
 */
@Service
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final ApartmentRepository apartmentRepository;
    private final MeterRepository meterRepository;

    public MeterReadingService(
        MeterReadingRepository meterReadingRepository,
        ApartmentRepository apartmentRepository,
        MeterRepository meterRepository
    ) {
        this.meterReadingRepository = meterReadingRepository;
        this.apartmentRepository = apartmentRepository;
        this.meterRepository = meterRepository;
    }

    /**
     * Tworzy nowy odczyt dla wskazanego licznika w danym lokalu.
     * Sprawdza duplikaty, regresję wartości oraz to, czy licznik jest aktywny
     * i rzeczywiście przypisany do wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowego odczytu
     * @return DTO z zapisanym odczytem
     * @throws NotFoundException jeśli lokal lub licznik nie istnieje
     * @throws BusinessValidationException jeśli odczyt jest duplikatem, wartość się cofa
     *         lub licznik nie należy do lokalu / jest nieaktywny
     */
    public MeterReadingResponse create(UUID apartmentId, MeterReadingRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .orElseThrow(() -> new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje"));

        Meter meter = resolveMeterForApartment(request.getMeterId(), apartmentId);

        if (!meter.isActive()) {
            throw new BusinessValidationException(
                "Licznik o ID " + meter.getId() + " jest nieaktywny — nie można dodać odczytu"
            );
        }

        checkDuplicateOnCreate(request);
        checkRegressionOnCreate(request);

        MeterReading reading = new MeterReading();
        reading.setApartment(apartment);
        reading.setMeter(meter);
        reading.setValue(request.getValue());
        reading.setReadingDate(request.getReadingDate());

        return toResponse(meterReadingRepository.save(reading));
    }

    /**
     * Pobiera stronicowaną listę odczytów dla wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param page numer strony (od 0)
     * @param size rozmiar strony
     * @return strona z odczytami
     * @throws NotFoundException jeśli lokal nie istnieje
     */
    public Page<MeterReadingResponse> getAllByApartment(UUID apartmentId, int page, int size) {
        // TODO: Implementacja weryfikacji uprawnień (Ownership/Authorization Check):
        // 1. Jeśli rola to MIESZKANIEC -> sprawdź czy jest przypisany do apartmentId.
        // 2. Jeśli rola to KONSERWATOR -> sprawdź czy ma aktywne zlecenie dla tego lokalu.
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("readingDate").descending());
        return meterReadingRepository
            .findByApartmentIdAndDeletedFalse(apartmentId, pageable)
            .map(this::toResponse);
    }

    /**
     * Pobiera pojedynczy odczyt licznika po identyfikatorze.
     *
     * @param id identyfikator odczytu
     * @return DTO z odczytem
     * @throws NotFoundException jeśli odczyt nie istnieje lub jest usunięty
     */
    public MeterReadingResponse getById(UUID id) {
        return toResponse(
            meterReadingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Odczyt licznika o ID " + id + " nie istnieje"))
        );
    }

    /**
     * Aktualizuje istniejący odczyt licznika.
     * Sprawdza duplikaty oraz regresję wartości z pominięciem aktualizowanego rekordu.
     *
     * @param id identyfikator odczytu do aktualizacji
     * @param request nowe dane odczytu
     * @return DTO ze zaktualizowanym odczytem
     * @throws NotFoundException jeśli odczyt lub licznik nie istnieje
     * @throws BusinessValidationException jeśli nowe dane naruszają reguły biznesowe
     */
    public MeterReadingResponse update(UUID id, MeterReadingRequest request) {
        MeterReading reading = meterReadingRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Odczyt licznika o ID " + id + " nie istnieje"));

        UUID apartmentId = reading.getApartment().getId();
        Meter meter = resolveMeterForApartment(request.getMeterId(), apartmentId);

        checkDuplicateOnUpdate(request, id);
        checkRegressionOnUpdate(request, id);

        reading.setMeter(meter);
        reading.setValue(request.getValue());
        reading.setReadingDate(request.getReadingDate());

        return toResponse(meterReadingRepository.save(reading));
    }

    /**
     * Miękko usuwa odczyt licznika (ustawia flagę deleted).
     *
     * @param id identyfikator odczytu do usunięcia
     * @throws NotFoundException jeśli odczyt nie istnieje
     */
    public void delete(UUID id) {
        MeterReading reading = meterReadingRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Odczyt licznika o ID " + id + " nie istnieje"));
        reading.setDeleted(true);
        meterReadingRepository.save(reading);
    }

    private Meter resolveMeterForApartment(UUID meterId, UUID apartmentId) {
        Meter meter = meterRepository.findById(meterId)
            .orElseThrow(() -> new NotFoundException("Licznik o ID " + meterId + " nie istnieje"));

        if (!meter.getApartment().getId().equals(apartmentId)) {
            throw new BusinessValidationException(
                "Licznik o ID " + meterId + " nie jest przypisany do lokalu " + apartmentId
            );
        }
        return meter;
    }

    private void checkDuplicateOnCreate(MeterReadingRequest request) {
        if (meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(
            request.getMeterId(), request.getReadingDate()
        )) {
            throw new BusinessValidationException(
                "Odczyt dla licznika " + request.getMeterId() + " z datą "
                    + request.getReadingDate() + " już istnieje"
            );
        }
    }

    private void checkDuplicateOnUpdate(MeterReadingRequest request, UUID currentId) {
        if (meterReadingRepository.existsByMeterIdAndReadingDateAndIdNotAndDeletedFalse(
            request.getMeterId(), request.getReadingDate(), currentId
        )) {
            throw new BusinessValidationException(
                "Odczyt dla licznika " + request.getMeterId() + " z datą "
                    + request.getReadingDate() + " już istnieje"
            );
        }
    }

    private void checkRegressionOnCreate(MeterReadingRequest request) {
        MeterReading latest = meterReadingRepository
            .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(request.getMeterId());

        if (latest == null) {
            return;
        }

        if (request.getValue().compareTo(latest.getValue()) < 0) {
            throw new BusinessValidationException(
                "Nowa wartość odczytu (" + request.getValue() + ") nie może być mniejsza niż ostatni odczyt ("
                    + latest.getValue() + ") z dnia " + latest.getReadingDate()
            );
        }
    }

    private void checkRegressionOnUpdate(MeterReadingRequest request, UUID currentId) {
        MeterReading latest = meterReadingRepository
            .findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(request.getMeterId());

        if (latest == null) {
            return;
        }

        if (!latest.getId().equals(currentId) && request.getValue().compareTo(latest.getValue()) < 0) {
            throw new BusinessValidationException(
                "Nowa wartość odczytu (" + request.getValue() + ") nie może być mniejsza niż ostatni odczyt ("
                    + latest.getValue() + ") z dnia " + latest.getReadingDate()
            );
        }
    }

    private MeterReadingResponse toResponse(MeterReading reading) {
        Meter meter = reading.getMeter();
        return new MeterReadingResponse(
            reading.getId(),
            reading.getApartment().getId(),
            meter.getId(),
            meter.getSerialNumber(),
            meter.getMediumType(),
            reading.getValue(),
            reading.getReadingDate(),
            reading.getCreatedAt(),
            reading.getUpdatedAt(),
            reading.getRecordedBy()
        );
    }
}

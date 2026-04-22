package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.MeterRequest;
import pl.edu.ur.blokur.dto.MeterResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Meter;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.MeterRepository;

import java.util.List;
import java.util.UUID;

/**
 * Serwis biznesowy obsługujący zarządzanie licznikami przypisanymi do lokali.
 */
@Service
public class MeterService {

    private final MeterRepository meterRepository;
    private final ApartmentRepository apartmentRepository;

    public MeterService(MeterRepository meterRepository, ApartmentRepository apartmentRepository) {
        this.meterRepository = meterRepository;
        this.apartmentRepository = apartmentRepository;
    }

    /**
     * Dodaje nowy licznik do wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowego licznika
     * @return DTO z utworzonym licznikiem
     * @throws NotFoundException jeśli lokal nie istnieje
     * @throws BusinessValidationException jeśli numer seryjny jest już używany
     */
    @Transactional
    public MeterResponse create(UUID apartmentId, MeterRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .orElseThrow(() -> new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje"));

        if (meterRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BusinessValidationException(
                "Licznik o numerze seryjnym '" + request.getSerialNumber() + "' już istnieje"
            );
        }

        Meter meter = new Meter();
        meter.setApartment(apartment);
        meter.setSerialNumber(request.getSerialNumber());
        meter.setMediumType(request.getMediumType());
        meter.setInstallationDate(request.getInstallationDate());
        meter.setActive(true);

        return toResponse(meterRepository.save(meter));
    }

    /**
     * Pobiera listę liczników przypisanych do wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return lista liczników (aktywnych i nieaktywnych)
     * @throws NotFoundException jeśli lokal nie istnieje
     */
    @Transactional(readOnly = true)
    public List<MeterResponse> getAllByApartment(UUID apartmentId) {
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje");
        }

        return meterRepository.findByApartmentId(apartmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Dezaktywuje licznik (ustawia is_active = false). Nie usuwa rekordu ani historii odczytów.
     *
     * @param meterId identyfikator licznika
     * @return DTO zdezaktywowanego licznika
     * @throws NotFoundException jeśli licznik nie istnieje
     * @throws BusinessValidationException jeśli licznik jest już nieaktywny
     */
    @Transactional
    public MeterResponse deactivate(UUID meterId) {
        Meter meter = meterRepository.findById(meterId)
            .orElseThrow(() -> new NotFoundException("Licznik o ID " + meterId + " nie istnieje"));

        if (!meter.isActive()) {
            throw new BusinessValidationException(
                "Licznik o ID " + meterId + " jest już nieaktywny"
            );
        }

        meter.setActive(false);
        return toResponse(meterRepository.save(meter));
    }

    private MeterResponse toResponse(Meter meter) {
        return new MeterResponse(
            meter.getId(),
            meter.getApartment().getId(),
            meter.getSerialNumber(),
            meter.getMediumType(),
            meter.getInstallationDate(),
            meter.isActive()
        );
    }
}

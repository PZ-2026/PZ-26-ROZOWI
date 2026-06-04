package pl.edu.ur.blokur.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.edu.ur.blokur.dto.MeterReadingRequest;
import pl.edu.ur.blokur.dto.MeterReadingResponse;
import pl.edu.ur.blokur.exception.BusinessValidationException;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Meter;
import pl.edu.ur.blokur.models.MeterReading;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.MeterReadingRepository;
import pl.edu.ur.blokur.repository.MeterRepository;
import pl.edu.ur.blokur.repository.TicketRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis biznesowy obsługujący logikę odczytów liczników. Zawiera walidację duplikatów, regresji
 * wartości oraz mapowanie DTO.
 */
@Service
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final ApartmentRepository apartmentRepository;
    private final MeterRepository meterRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param meterReadingRepository repozytorium odczytów liczników
     * @param apartmentRepository repozytorium lokali
     * @param meterRepository repozytorium liczników
     * @param userRepository repozytorium użytkowników
     * @param ticketRepository repozytorium zgłoszeń
     */
    public MeterReadingService(
            MeterReadingRepository meterReadingRepository,
            ApartmentRepository apartmentRepository,
            MeterRepository meterRepository,
            UserRepository userRepository,
            TicketRepository ticketRepository) {
        this.meterReadingRepository = meterReadingRepository;
        this.apartmentRepository = apartmentRepository;
        this.meterRepository = meterRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Tworzy nowy odczyt dla wskazanego licznika w danym lokalu. Sprawdza duplikaty, regresję
     * wartości oraz to, czy licznik jest aktywny i rzeczywiście przypisany do wskazanego lokalu.
     * Konserwator może dodawać odczyty wyłącznie dla lokali, do których ma przypisane zgłoszenie.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowego odczytu
     * @param username email zalogowanego użytkownika
     * @return DTO z zapisanym odczytem
     * @throws NotFoundException jeśli lokal lub licznik nie istnieje
     * @throws BusinessValidationException jeśli odczyt jest duplikatem, wartość się cofa lub
     *     licznik nie należy do lokalu / jest nieaktywny, bądź konserwator nie ma dostępu
     */
    public MeterReadingResponse create(UUID apartmentId, MeterReadingRequest request, String username) {
        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if ("KONSERWATOR".equals(user.getRole())
                && !ticketRepository.existsByAssignedToIdAndApartmentId(user.getId(), apartmentId)) {
            throw new BusinessValidationException(
                    "Brak przypisanego zgłoszenia dla tego lokalu — konserwator nie jest upoważniony");
        }

        var apartment =
                apartmentRepository
                        .findById(apartmentId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Lokal o ID " + apartmentId + " nie istnieje"));

        var meter = resolveMeterForApartment(request.getMeterId(), apartmentId);

        if (!meter.isActive()) {
            throw new BusinessValidationException(
                    "Licznik o ID " + meter.getId() + " jest nieaktywny — nie można dodać odczytu");
        }

        checkDuplicateOnCreate(request);
        checkRegressionOnCreate(request);

        var reading = new MeterReading();
        reading.setApartment(apartment);
        reading.setMeter(meter);
        reading.setValue(request.getValue());
        reading.setReadingDate(request.getReadingDate());

        return toResponse(meterReadingRepository.save(reading));
    }

    /**
     * Pobiera stronicowaną listę odczytów dla wskazanego lokalu. Mieszkaniec może pobierać odczyty
     * wyłącznie swojego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @param page numer strony (od 0)
     * @param size rozmiar strony
     * @param username email zalogowanego użytkownika
     * @return strona z odczytami
     * @throws NotFoundException jeśli lokal nie istnieje
     * @throws BusinessValidationException jeśli mieszkaniec nie jest najemcą tego lokalu
     */
    public Page<MeterReadingResponse> getAllByApartment(UUID apartmentId, UUID meterId, int page, int size, String username) {
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new NotFoundException("Lokal o ID " + apartmentId + " nie istnieje");
        }

        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        if ("MIESZKANIEC".equals(user.getRole())) {
            boolean isTenant =
                    user.getUserApartments().stream()
                            .anyMatch(ua -> ua.getApartment().getId().equals(apartmentId));
            if (!isTenant) {
                throw new BusinessValidationException(
                        "Brak dostępu — lokal nie należy do zalogowanego mieszkańca");
            }
        }

        var pageable = PageRequest.of(page, size, Sort.by("readingDate").descending());
        
        Page<MeterReading> readingsPage;
        if (meterId != null) {
            readingsPage = meterReadingRepository.findByApartmentIdAndMeterIdAndDeletedFalse(apartmentId, meterId, pageable);
        } else {
            readingsPage = meterReadingRepository.findByApartmentIdAndDeletedFalse(apartmentId, pageable);
        }
        
        return readingsPage.map(this::toResponse);
    }

    /**
     * Pobiera pojedynczy odczyt licznika po identyfikatorze. Mieszkaniec i konserwator mają dostęp
     * wyłącznie do odczytów lokali, do których mają uprawnienia.
     *
     * @param id identyfikator odczytu
     * @param username email zalogowanego użytkownika
     * @return DTO z odczytem
     * @throws NotFoundException jeśli odczyt nie istnieje lub jest usunięty
     * @throws BusinessValidationException jeśli użytkownik nie ma dostępu do tego odczytu
     */
    public MeterReadingResponse getById(UUID id, String username) {
        var reading =
                meterReadingRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Odczyt licznika o ID " + id + " nie istnieje"));

        var user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        UUID apartmentId = reading.getApartment().getId();

        if ("MIESZKANIEC".equals(user.getRole())) {
            boolean isTenant =
                    user.getUserApartments().stream()
                            .anyMatch(ua -> ua.getApartment().getId().equals(apartmentId));
            if (!isTenant) {
                throw new BusinessValidationException(
                        "Brak dostępu — lokal nie należy do zalogowanego mieszkańca");
            }
        } else if ("KONSERWATOR".equals(user.getRole())) {
            if (!ticketRepository.existsByAssignedToIdAndApartmentId(user.getId(), apartmentId)) {
                throw new BusinessValidationException(
                        "Brak przypisanego zgłoszenia dla tego lokalu");
            }
        }

        return toResponse(reading);
    }

    /**
     * Aktualizuje istniejący odczyt licznika. Sprawdza duplikaty oraz regresję wartości z
     * pominięciem aktualizowanego rekordu.
     *
     * @param id identyfikator odczytu do aktualizacji
     * @param request nowe dane odczytu
     * @return DTO ze zaktualizowanym odczytem
     * @throws NotFoundException jeśli odczyt lub licznik nie istnieje
     * @throws BusinessValidationException jeśli nowe dane naruszają reguły biznesowe
     */
    public MeterReadingResponse update(UUID id, MeterReadingRequest request) {
        var reading =
                meterReadingRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Odczyt licznika o ID " + id + " nie istnieje"));

        var apartmentId = reading.getApartment().getId();
        var meter = resolveMeterForApartment(request.getMeterId(), apartmentId);

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
        var reading =
                meterReadingRepository
                        .findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Odczyt licznika o ID " + id + " nie istnieje"));
        reading.setDeleted(true);
        meterReadingRepository.save(reading);
    }

    private Meter resolveMeterForApartment(UUID meterId, UUID apartmentId) {
        var meter =
                meterRepository
                        .findById(meterId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Licznik o ID " + meterId + " nie istnieje"));

        if (!meter.getApartment().getId().equals(apartmentId)) {
            throw new BusinessValidationException(
                    "Licznik o ID " + meterId + " nie jest przypisany do lokalu " + apartmentId);
        }
        return meter;
    }

    private void checkDuplicateOnCreate(MeterReadingRequest request) {
        if (meterReadingRepository.existsByMeterIdAndReadingDateAndDeletedFalse(
                request.getMeterId(), request.getReadingDate())) {
            throw new BusinessValidationException(
                    "Odczyt dla licznika "
                            + request.getMeterId()
                            + " z datą "
                            + request.getReadingDate()
                            + " już istnieje");
        }
    }

    private void checkDuplicateOnUpdate(MeterReadingRequest request, UUID currentId) {
        if (meterReadingRepository.existsByMeterIdAndReadingDateAndIdNotAndDeletedFalse(
                request.getMeterId(), request.getReadingDate(), currentId)) {
            throw new BusinessValidationException(
                    "Odczyt dla licznika "
                            + request.getMeterId()
                            + " z datą "
                            + request.getReadingDate()
                            + " już istnieje");
        }
    }

    private void checkRegressionOnCreate(MeterReadingRequest request) {
        var latest =
                meterReadingRepository.findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(
                        request.getMeterId());

        if (latest == null) {
            return;
        }

        if (request.getValue().compareTo(latest.getValue()) < 0) {
            throw new BusinessValidationException(
                    "Nowa wartość odczytu ("
                            + request.getValue()
                            + ") nie może być mniejsza niż ostatni odczyt ("
                            + latest.getValue()
                            + ") z dnia "
                            + latest.getReadingDate());
        }
    }

    private void checkRegressionOnUpdate(MeterReadingRequest request, UUID currentId) {
        var latest =
                meterReadingRepository.findTopByMeterIdAndDeletedFalseOrderByReadingDateDesc(
                        request.getMeterId());

        if (latest == null) {
            return;
        }

        if (!latest.getId().equals(currentId)
                && request.getValue().compareTo(latest.getValue()) < 0) {
            throw new BusinessValidationException(
                    "Nowa wartość odczytu ("
                            + request.getValue()
                            + ") nie może być mniejsza niż ostatni odczyt ("
                            + latest.getValue()
                            + ") z dnia "
                            + latest.getReadingDate());
        }
    }

    private MeterReadingResponse toResponse(MeterReading reading) {
        var meter = reading.getMeter();
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
                reading.getRecordedBy());
    }
}

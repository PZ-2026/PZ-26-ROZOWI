package pl.edu.ur.blokur.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;

/**
 * Serwis zestawienia sald i zaległości wszystkich lokali dla widoku zarządcy. Pobiera aktualne
 * salda, daty ostatnich wpłat i wylicza dni zalegania, a następnie stosuje filtry i sortowanie.
 */
@Service
public class ApartmentBalanceService {

    private final ApartmentRepository apartmentRepository;
    private final FinancialTransactionRepository transactionRepository;

    /**
     * Tworzy serwis z wymaganymi zależnościami.
     *
     * @param apartmentRepository repozytorium lokali
     * @param transactionRepository repozytorium transakcji finansowych
     */
    public ApartmentBalanceService(
            ApartmentRepository apartmentRepository,
            FinancialTransactionRepository transactionRepository) {
        this.apartmentRepository = apartmentRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Zwraca zestawienie sald lokali z opcjonalnym filtrowaniem i sortowaniem.
     *
     * <p>Filtry:
     *
     * <ul>
     *   <li>{@code propertyId} — ogranicza wyniki do lokali danej nieruchomości
     *   <li>{@code minDebt} — pokazuje tylko lokale z zaległością co najmniej {@code minDebt} PLN
     *       (saldo ≤ −minDebt)
     *   <li>{@code minDaysOverdue} — pokazuje tylko lokale z liczbą dni od ostatniej wpłaty ≥
     *       {@code minDaysOverdue}
     * </ul>
     *
     * <p>Sortowanie domyślne: malejąco po kwocie zadłużenia (saldo rosnąco).
     *
     * @param propertyId identyfikator nieruchomości lub {@code null} — wszystkie nieruchomości
     * @param minDebt minimalna kwota zaległości w PLN lub {@code null} — bez filtru
     * @param minDaysOverdue minimalna liczba dni zalegania lub {@code null} — bez filtru
     * @param sortDesc {@code true} — malejąco po zaległości (domyślnie); {@code false} — rosnąco
     * @return posortowana i przefiltrowana lista sald lokali
     */
    @Transactional(readOnly = true)
    public List<ApartmentBalanceResponse> getBalances(
            UUID propertyId, BigDecimal minDebt, Long minDaysOverdue, boolean sortDesc) {

        var apartments =
                propertyId != null
                        ? apartmentRepository.findAllByPropertyId(propertyId)
                        : apartmentRepository.findAllWithBuilding();

        if (apartments.isEmpty()) {
            return List.of();
        }

        var apartmentIds = apartments.stream().map(Apartment::getId).toList();

        var lastPaymentDates = buildLastPaymentMap(apartmentIds);
        var today = LocalDate.now();

        var byDebt = Comparator.comparing(ApartmentBalanceResponse::getBalance);
        var effectiveComparator = sortDesc ? byDebt : byDebt.reversed();

        return apartments.stream()
                .map(apt -> toResponse(apt, lastPaymentDates, today))
                .filter(r -> passesDebtFilter(r, minDebt))
                .filter(r -> passesDaysFilter(r, minDaysOverdue))
                .sorted(effectiveComparator)
                .toList();
    }

    private Map<UUID, LocalDate> buildLastPaymentMap(List<UUID> apartmentIds) {
        return transactionRepository.findLastPaymentDatesByApartmentIds(apartmentIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (LocalDate) row[1]));
    }

    private ApartmentBalanceResponse toResponse(
            Apartment apt, Map<UUID, LocalDate> lastPaymentDates, LocalDate today) {

        var address =
                "%s m. %s"
                        .formatted(apt.getStaircase().getBuilding().getAddress(), apt.getNumber());

        var balance = apt.getCurrentBalance() != null ? apt.getCurrentBalance() : BigDecimal.ZERO;
        var lastPayment = lastPaymentDates.get(apt.getId());
        var daysOverdue = lastPayment != null ? ChronoUnit.DAYS.between(lastPayment, today) : null;
        return new ApartmentBalanceResponse(
                apt.getId(), address, balance, lastPayment, daysOverdue);
    }

    private boolean passesDebtFilter(ApartmentBalanceResponse r, BigDecimal minDebt) {
        if (minDebt == null) return true;
        return r.getBalance().compareTo(minDebt.negate()) <= 0;
    }

    private boolean passesDaysFilter(ApartmentBalanceResponse r, Long minDaysOverdue) {
        if (minDaysOverdue == null) return true;
        return r.getDaysOverdue() != null && r.getDaysOverdue() >= minDaysOverdue;
    }
}

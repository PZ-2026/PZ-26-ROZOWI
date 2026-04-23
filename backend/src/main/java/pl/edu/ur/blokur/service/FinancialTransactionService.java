package pl.edu.ur.blokur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.ApartmentTransactionsResponse;
import pl.edu.ur.blokur.dto.FinancialTransactionRequest;
import pl.edu.ur.blokur.dto.FinancialTransactionResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.FinancialTransaction;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;
import pl.edu.ur.blokur.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serwis biznesowy obsługujący logikę transakcji finansowych lokali.
 * Odpowiada za pobieranie historii transakcji, tworzenie nowych operacji
 * oraz atomową aktualizację salda lokalu.
 */
@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;

    public FinancialTransactionService(
        FinancialTransactionRepository financialTransactionRepository,
        ApartmentRepository apartmentRepository,
        UserRepository userRepository
    ) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Pobiera historię transakcji oraz zbuforowane saldo dla wskazanego lokalu.
     *
     * @param apartmentId identyfikator lokalu
     * @return DTO zawierające saldo i listę transakcji
     * @throws NotFoundException jeśli lokal nie istnieje
     */
    public ApartmentTransactionsResponse getTransactionsForApartment(UUID apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .orElseThrow(() -> new NotFoundException(
                "Lokal o ID " + apartmentId + " nie istnieje"));

        List<FinancialTransactionResponse> transactions = financialTransactionRepository
            .findByApartmentIdOrderByTransactionDateDesc(apartmentId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return new ApartmentTransactionsResponse(
            apartment.getCurrentBalance(),
            transactions
        );
    }

    /**
     * Tworzy nową transakcję finansową dla wskazanego lokalu
     * i aktualizuje jego saldo ({@code currentBalance}).
     *
     * <p>Operacja jest atomowa — zapis transakcji i aktualizacja salda
     * wykonywane są w ramach jednej transakcji bazodanowej.</p>
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowej transakcji
     * @param userEmail adres e-mail użytkownika rejestrującego transakcję
     * @return DTO z zapisaną transakcją
     * @throws NotFoundException jeśli lokal lub użytkownik nie istnieje
     */
    @Transactional
    public FinancialTransactionResponse createTransaction(
        UUID apartmentId,
        FinancialTransactionRequest request,
        String userEmail
    ) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
            .orElseThrow(() -> new NotFoundException(
                "Lokal o ID " + apartmentId + " nie istnieje"));

        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException(
                "Użytkownik o adresie " + userEmail + " nie istnieje"));

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setApartment(apartment);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setRecordedBy(user);

        FinancialTransaction saved = financialTransactionRepository.save(transaction);

        BigDecimal currentBalance = apartment.getCurrentBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        apartment.setCurrentBalance(currentBalance.add(request.getAmount()));
        apartmentRepository.save(apartment);

        return toResponse(saved);
    }

    /**
     * Mapuje encję {@link FinancialTransaction} na DTO odpowiedzi.
     *
     * @param transaction encja transakcji
     * @return DTO transakcji
     */
    private FinancialTransactionResponse toResponse(FinancialTransaction transaction) {
        return new FinancialTransactionResponse(
            transaction.getId(),
            transaction.getApartment().getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getRecordedBy().getEmail()
        );
    }
}

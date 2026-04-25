package pl.edu.ur.blokur.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.ApartmentTransactionsResponse;
import pl.edu.ur.blokur.dto.CsvImportErrorDto;
import pl.edu.ur.blokur.dto.CsvImportResultDto;
import pl.edu.ur.blokur.dto.FinancialTransactionRequest;
import pl.edu.ur.blokur.dto.FinancialTransactionResponse;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.FinancialTransaction;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis biznesowy obsługujący logikę transakcji finansowych lokali. Odpowiada za pobieranie
 * historii transakcji, tworzenie nowych operacji oraz atomową aktualizację salda lokalu.
 */
@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;

    public FinancialTransactionService(
            FinancialTransactionRepository financialTransactionRepository,
            ApartmentRepository apartmentRepository,
            UserRepository userRepository) {
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
        Apartment apartment =
                apartmentRepository
                        .findById(apartmentId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Lokal o ID " + apartmentId + " nie istnieje"));

        List<FinancialTransactionResponse> transactions =
                financialTransactionRepository
                        .findByApartmentIdOrderByTransactionDateDesc(apartmentId)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return new ApartmentTransactionsResponse(apartment.getCurrentBalance(), transactions);
    }

    /**
     * Tworzy nową transakcję finansową dla wskazanego lokalu i aktualizuje jego saldo ({@code
     * currentBalance}).
     *
     * <p>Operacja jest atomowa — zapis transakcji i aktualizacja salda wykonywane są w ramach
     * jednej transakcji bazodanowej.
     *
     * @param apartmentId identyfikator lokalu
     * @param request dane nowej transakcji
     * @param userEmail adres e-mail użytkownika rejestrującego transakcję
     * @return DTO z zapisaną transakcją
     * @throws NotFoundException jeśli lokal lub użytkownik nie istnieje
     */
    @Transactional
    public FinancialTransactionResponse createTransaction(
            UUID apartmentId, FinancialTransactionRequest request, String userEmail) {
        Apartment apartment =
                apartmentRepository
                        .findById(apartmentId)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Lokal o ID " + apartmentId + " nie istnieje"));

        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Użytkownik o adresie "
                                                        + userEmail
                                                        + " nie istnieje"));

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
                transaction.getRecordedBy().getEmail());
    }

    /**
     * Importuje transakcje finansowe z pliku CSV. Przetwarza wiersz po wierszu - zapisuje poprawne
     * i zbiera błędy.
     *
     * @param file plik CSV
     * @param userEmail adres e-mail użytkownika zlecającego import
     * @return podsumowanie zaimportowanych wierszy i błędów
     */
    public CsvImportResultDto importTransactionsFromCsv(MultipartFile file, String userEmail) {
        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Użytkownik o adresie "
                                                        + userEmail
                                                        + " nie istnieje"));

        List<CsvImportErrorDto> errors = new ArrayList<>();
        int importedCount = 0;
        int errorCount = 0;

        Set<String> allowedTypes = new HashSet<>(Arrays.asList("WPLATA", "NALICZENIE", "KOREKTA"));

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser csvParser =
                        new CSVParser(
                                reader,
                                CSVFormat.DEFAULT
                                        .builder()
                                        .setHeader(
                                                "apartment_id",
                                                "date",
                                                "type",
                                                "amount",
                                                "description")
                                        .setSkipHeaderRecord(true)
                                        .setIgnoreHeaderCase(true)
                                        .setTrim(true)
                                        .build())) {

            for (CSVRecord csvRecord : csvParser) {
                int lineNumber =
                        (int) csvRecord.getRecordNumber()
                                + 1; // getRecordNumber starts at 1, but we skip header, wait,
                // getRecordNumber counts lines processed

                try {
                    String apartmentIdStr = csvRecord.get("apartment_id");
                    String dateStr = csvRecord.get("date");
                    String type = csvRecord.get("type");
                    String amountStr = csvRecord.get("amount");
                    String description = csvRecord.get("description");

                    // Validate UUID
                    UUID apartmentId;
                    try {
                        apartmentId = UUID.fromString(apartmentIdStr);
                    } catch (IllegalArgumentException e) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber, "Nieprawidłowy format apartment_id"));
                        errorCount++;
                        continue;
                    }

                    // Validate Date
                    LocalDate transactionDate;
                    try {
                        transactionDate = LocalDate.parse(dateStr);
                    } catch (DateTimeParseException e) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber,
                                        "Nieprawidłowy format daty (wymagany ISO np. YYYY-MM-DD)"));
                        errorCount++;
                        continue;
                    }

                    // Validate Type
                    if (!allowedTypes.contains(type.toUpperCase())) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber,
                                        "Nieznany typ operacji (dozwolone: WPLATA, NALICZENIE,"
                                                + " KOREKTA)"));
                        errorCount++;
                        continue;
                    }

                    // Validate Amount
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (NumberFormatException e) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber, "Kwota musi być poprawną liczbą (np. 150.50)"));
                        errorCount++;
                        continue;
                    }

                    // Validate Apartment existence
                    Apartment apartment = apartmentRepository.findById(apartmentId).orElse(null);
                    if (apartment == null) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber, "Lokal o podanym ID nie istnieje"));
                        errorCount++;
                        continue;
                    }

                    // Proceed to save inside an isolated try block in case DB fails
                    try {
                        saveImportedTransaction(
                                apartment,
                                type.toUpperCase(),
                                amount,
                                description,
                                transactionDate,
                                user);
                        importedCount++;
                    } catch (Exception e) {
                        errors.add(
                                new CsvImportErrorDto(
                                        lineNumber,
                                        "Błąd podczas zapisu do bazy danych: " + e.getMessage()));
                        errorCount++;
                    }

                } catch (IllegalArgumentException e) {
                    errors.add(
                            new CsvImportErrorDto(
                                    lineNumber,
                                    "Błąd odczytu wiersza (prawdopodobnie brak wymaganej liczby"
                                            + " kolumn)"));
                    errorCount++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas odczytu pliku CSV: " + e.getMessage(), e);
        }

        return new CsvImportResultDto(importedCount, errorCount, errors);
    }

    @Transactional
    protected void saveImportedTransaction(
            Apartment apartment,
            String type,
            BigDecimal amount,
            String description,
            LocalDate transactionDate,
            User user) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setApartment(apartment);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(transactionDate);
        transaction.setRecordedBy(user);

        financialTransactionRepository.save(transaction);

        BigDecimal currentBalance = apartment.getCurrentBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        apartment.setCurrentBalance(currentBalance.add(amount));
        apartmentRepository.save(apartment);
    }
}

package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.ur.blokur.dto.ApartmentTransactionsResponse;
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
 * Testy jednostkowe dla {@link FinancialTransactionService}. Weryfikują logikę biznesową transakcji
 * finansowych: tworzenie transakcji z atomową aktualizacją salda oraz pobieranie historii
 * transakcji lokalu.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialTransactionService — serwis transakcji finansowych")
class FinancialTransactionServiceTest {

    @Mock private FinancialTransactionRepository financialTransactionRepository;

    @Mock private ApartmentRepository apartmentRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private FinancialTransactionService financialTransactionService;

    private UUID apartmentId;
    private Apartment apartment;
    private User user;
    private FinancialTransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("1");
        apartment.setCurrentBalance(new BigDecimal("200.00"));

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("mock.zarzadca1@blokur.pl");

        validRequest =
                new FinancialTransactionRequest(
                        "WPLATA",
                        new BigDecimal("500.00"),
                        "Wpłata czynszu za kwiecień 2026",
                        LocalDate.of(2026, 4, 15));
    }

    // =======================================================
    // CREATE TRANSACTION
    // =======================================================

    @Nested
    @DisplayName("createTransaction()")
    class CreateTransactionTests {

        @Test
        @DisplayName("Poprawne dane — zapisuje transakcję i aktualizuje saldo")
        void shouldCreateTransactionAndUpdateBalance() {
            FinancialTransaction saved = new FinancialTransaction();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setType(validRequest.getType());
            saved.setAmount(validRequest.getAmount());
            saved.setDescription(validRequest.getDescription());
            saved.setTransactionDate(validRequest.getTransactionDate());
            saved.setRecordedBy(user);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                    .thenReturn(saved);

            FinancialTransactionResponse response =
                    financialTransactionService.createTransaction(
                            apartmentId, validRequest, user.getEmail());

            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo("WPLATA");
            assertThat(response.getAmount()).isEqualByComparingTo("500.00");
            assertThat(response.getApartmentId()).isEqualTo(apartmentId);
            assertThat(response.getRecordedByEmail()).isEqualTo(user.getEmail());

            assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("700.00");

            verify(financialTransactionRepository).save(any(FinancialTransaction.class));
            verify(apartmentRepository).save(apartment);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundWhenApartmentDoesNotExist() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    financialTransactionService.createTransaction(
                                            apartmentId, validRequest, user.getEmail()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());

            verify(financialTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail("nieznany@test.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    financialTransactionService.createTransaction(
                                            apartmentId, validRequest, "nieznany@test.pl"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("nieznany@test.pl");

            verify(financialTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Saldo null — traktowane jako zero, prawidłowo dodaje kwotę")
        void shouldHandleNullCurrentBalance() {
            apartment.setCurrentBalance(null);

            FinancialTransaction saved = new FinancialTransaction();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setType(validRequest.getType());
            saved.setAmount(validRequest.getAmount());
            saved.setDescription(validRequest.getDescription());
            saved.setTransactionDate(validRequest.getTransactionDate());
            saved.setRecordedBy(user);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                    .thenReturn(saved);

            financialTransactionService.createTransaction(
                    apartmentId, validRequest, user.getEmail());

            assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("Kwota ujemna (naliczenie) — saldo maleje")
        void shouldDecreaseBalanceForNegativeAmount() {
            FinancialTransactionRequest chargeRequest =
                    new FinancialTransactionRequest(
                            "NALICZENIE",
                            new BigDecimal("-300.00"),
                            "Naliczenie czynszu za kwiecień",
                            LocalDate.of(2026, 4, 1));

            FinancialTransaction saved = new FinancialTransaction();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setType(chargeRequest.getType());
            saved.setAmount(chargeRequest.getAmount());
            saved.setDescription(chargeRequest.getDescription());
            saved.setTransactionDate(chargeRequest.getTransactionDate());
            saved.setRecordedBy(user);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                    .thenReturn(saved);

            financialTransactionService.createTransaction(
                    apartmentId, chargeRequest, user.getEmail());

            assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("-100.00");
        }
    }

    // =======================================================
    // GET TRANSACTIONS FOR APARTMENT
    // =======================================================

    @Nested
    @DisplayName("getTransactionsForApartment()")
    class GetTransactionsTests {

        private static final String ZARZADCA_EMAIL = "zarzadca@blokur.pl";
        private static final String MIESZKANIEC_EMAIL = "mieszkaniec@blokur.pl";

        private User zarzadca;
        private User mieszkaniec;

        @BeforeEach
        void setUpUsers() {
            zarzadca = new User();
            zarzadca.setId(UUID.randomUUID());
            zarzadca.setEmail(ZARZADCA_EMAIL);
            zarzadca.setRole("ZARZADCA");

            mieszkaniec = new User();
            mieszkaniec.setId(UUID.randomUUID());
            mieszkaniec.setEmail(MIESZKANIEC_EMAIL);
            mieszkaniec.setRole("MIESZKANIEC");
        }

        @Test
        @DisplayName("Zarządca — zwraca pełną historię (bez limitu 24 miesięcy)")
        void givenZarzadca_shouldReturnFullHistory() {
            FinancialTransaction transaction = new FinancialTransaction();
            transaction.setId(UUID.randomUUID());
            transaction.setApartment(apartment);
            transaction.setType("WPLATA");
            transaction.setAmount(new BigDecimal("500.00"));
            transaction.setDescription("Wpłata czynszu");
            transaction.setTransactionDate(LocalDate.of(2026, 4, 15));
            transaction.setRecordedBy(zarzadca);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(
                            apartmentId))
                    .thenReturn(List.of(transaction));

            ApartmentTransactionsResponse response =
                    financialTransactionService.getTransactionsForApartment(
                            apartmentId, ZARZADCA_EMAIL);

            assertThat(response.getCurrentBalance()).isEqualByComparingTo("200.00");
            assertThat(response.getTransactions()).hasSize(1);
            assertThat(response.getTransactions().get(0).getType()).isEqualTo("WPLATA");
            verify(financialTransactionRepository)
                    .findByApartmentIdOrderByTransactionDateDesc(apartmentId);
            verify(financialTransactionRepository, never())
                    .findByApartmentIdAndTransactionDateAfter(any(), any());
        }

        @Test
        @DisplayName("Mieszkaniec — używa zapytania z limitem 24 miesięcy")
        void givenMieszkaniec_shouldApply24MonthFilter() {
            FinancialTransaction recent = new FinancialTransaction();
            recent.setId(UUID.randomUUID());
            recent.setApartment(apartment);
            recent.setType("WPLATA");
            recent.setAmount(new BigDecimal("300.00"));
            recent.setDescription("Wpłata z ostatnich 24 miesięcy");
            recent.setTransactionDate(LocalDate.now().minusMonths(6));
            recent.setRecordedBy(mieszkaniec);

            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(financialTransactionRepository.findByApartmentIdAndTransactionDateAfter(
                            eq(apartmentId), any(LocalDate.class)))
                    .thenReturn(List.of(recent));

            ApartmentTransactionsResponse response =
                    financialTransactionService.getTransactionsForApartment(
                            apartmentId, MIESZKANIEC_EMAIL);

            assertThat(response.getTransactions()).hasSize(1);
            assertThat(response.getTransactions().get(0).getType()).isEqualTo("WPLATA");
            verify(financialTransactionRepository)
                    .findByApartmentIdAndTransactionDateAfter(
                            eq(apartmentId), any(LocalDate.class));
            verify(financialTransactionRepository, never())
                    .findByApartmentIdOrderByTransactionDateDesc(any());
        }

        @Test
        @DisplayName("Mieszkaniec — data graniczna to 24 miesiące wstecz od dziś")
        void givenMieszkaniec_cutoffShouldBe24MonthsAgo() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(MIESZKANIEC_EMAIL))
                    .thenReturn(Optional.of(mieszkaniec));
            when(financialTransactionRepository.findByApartmentIdAndTransactionDateAfter(
                            any(), any()))
                    .thenReturn(List.of());

            financialTransactionService.getTransactionsForApartment(
                    apartmentId, MIESZKANIEC_EMAIL);

            LocalDate expectedCutoff = LocalDate.now().minusMonths(24);
            verify(financialTransactionRepository)
                    .findByApartmentIdAndTransactionDateAfter(
                            eq(apartmentId),
                            org.mockito.ArgumentMatchers.argThat(
                                    cutoff ->
                                            !cutoff.isAfter(expectedCutoff)
                                                    && !cutoff.isBefore(
                                                            expectedCutoff.minusDays(1))));
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentApartment() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    financialTransactionService.getTransactionsForApartment(
                                            apartmentId, ZARZADCA_EMAIL))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(apartmentId.toString());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentUser() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail("ghost@blokur.pl")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    financialTransactionService.getTransactionsForApartment(
                                            apartmentId, "ghost@blokur.pl"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("ghost@blokur.pl");
        }

        @Test
        @DisplayName("Zarządca — lokal bez transakcji zwraca puste saldo")
        void givenZarzadca_shouldReturnEmptyListWhenNoTransactions() {
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(ZARZADCA_EMAIL)).thenReturn(Optional.of(zarzadca));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(
                            apartmentId))
                    .thenReturn(List.of());

            ApartmentTransactionsResponse response =
                    financialTransactionService.getTransactionsForApartment(
                            apartmentId, ZARZADCA_EMAIL);

            assertThat(response.getCurrentBalance()).isEqualByComparingTo("200.00");
            assertThat(response.getTransactions()).isEmpty();
        }
    }

    // =======================================================
    // IMPORT TRANSACTIONS FROM CSV
    // =======================================================

    @Nested
    @DisplayName("importTransactionsFromCsv()")
    class ImportTransactionsFromCsvTests {

        @Test
        @DisplayName("Poprawny plik CSV - importuje wszystkie wiersze")
        void shouldImportValidCsv() {
            String csvContent =
                    "apartment_id,date,type,amount,description\n"
                            + apartmentId.toString()
                            + ",2026-04-15,WPLATA,150.00,Czynsz za kwiecień\n"
                            + apartmentId.toString()
                            + ",2026-04-16,NALICZENIE,-50.00,Opłata dodatkowa\n";
            MultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "test.csv",
                            "text/csv",
                            csvContent.getBytes(StandardCharsets.UTF_8));

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

            CsvImportResultDto result =
                    financialTransactionService.importTransactionsFromCsv(file, user.getEmail());

            assertThat(result.getImportedCount()).isEqualTo(2);
            assertThat(result.getErrorCount()).isEqualTo(0);
            assertThat(result.getErrors()).isEmpty();

            // Saldo końcowe powinno wynosić 200.00 + 150.00 - 50.00 = 300.00
            assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("300.00");
            // should be verified exactly 2 times
            verify(financialTransactionRepository, org.mockito.Mockito.times(2))
                    .save(any(FinancialTransaction.class));
            verify(apartmentRepository, org.mockito.Mockito.times(2)).save(apartment);
        }

        @Test
        @DisplayName("Częściowo błędny plik CSV - importuje tylko poprawne, zbiera błędy")
        void shouldImportValidAndCollectErrors() {
            String csvContent =
                    "apartment_id,date,type,amount,description\n"
                            + apartmentId.toString()
                            + ",2026-04-15,WPLATA,150.00,Czynsz\n"
                            + // line 2 - valid
                            "invalid-uuid,2026-04-16,WPLATA,50.00,Błąd UUID\n"
                            + // line 3 - invalid UUID
                            apartmentId.toString()
                            + ",invalid-date,WPLATA,50.00,Błąd daty\n"
                            + // line 4 - invalid date
                            apartmentId.toString()
                            + ",2026-04-16,ZLY_TYP,50.00,Błąd typu\n"
                            + // line 5 - invalid type
                            apartmentId.toString()
                            + ",2026-04-16,WPLATA,not-a-number,Błąd kwoty\n"; // line 6 - invalid
            // amount

            MultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "test.csv",
                            "text/csv",
                            csvContent.getBytes(StandardCharsets.UTF_8));

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

            CsvImportResultDto result =
                    financialTransactionService.importTransactionsFromCsv(file, user.getEmail());

            assertThat(result.getImportedCount()).isEqualTo(1);
            assertThat(result.getErrorCount()).isEqualTo(4);
            assertThat(result.getErrors()).hasSize(4);

            assertThat(result.getErrors().get(0).getLine()).isEqualTo(3);
            assertThat(result.getErrors().get(1).getLine()).isEqualTo(4);
            assertThat(result.getErrors().get(2).getLine()).isEqualTo(5);
            assertThat(result.getErrors().get(3).getLine()).isEqualTo(6);

            assertThat(apartment.getCurrentBalance()).isEqualByComparingTo("350.00");
            verify(financialTransactionRepository, org.mockito.Mockito.times(1))
                    .save(any(FinancialTransaction.class));
        }

        @Test
        @DisplayName("Nieistniejący użytkownik rzuca wyjątek")
        void shouldThrowExceptionWhenUserNotFound() {
            MultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "test.csv",
                            "text/csv",
                            "content".getBytes(StandardCharsets.UTF_8));
            when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    financialTransactionService.importTransactionsFromCsv(
                                            file, "nonexistent@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("nie istnieje");
        }
    }
}

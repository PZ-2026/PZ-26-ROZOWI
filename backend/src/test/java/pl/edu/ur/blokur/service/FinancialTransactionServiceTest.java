package pl.edu.ur.blokur.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy jednostkowe dla {@link FinancialTransactionService}.
 * Weryfikują logikę biznesową transakcji finansowych: tworzenie transakcji
 * z atomową aktualizacją salda oraz pobieranie historii transakcji lokalu.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialTransactionService — serwis transakcji finansowych")
class FinancialTransactionServiceTest {

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialTransactionService financialTransactionService;

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

        validRequest = new FinancialTransactionRequest(
            "WPLATA",
            new BigDecimal("500.00"),
            "Wpłata czynszu za kwiecień 2026",
            LocalDate.of(2026, 4, 15)
        );
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

            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
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

            assertThat(apartment.getCurrentBalance())
                .isEqualByComparingTo("700.00");

            verify(financialTransactionRepository).save(any(FinancialTransaction.class));
            verify(apartmentRepository).save(apartment);
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundWhenApartmentDoesNotExist() {
            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                financialTransactionService.createTransaction(
                    apartmentId, validRequest, user.getEmail()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(apartmentId.toString());

            verify(financialTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nieistniejący użytkownik — rzuca NotFoundException")
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail("nieznany@test.pl"))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
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

            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
            when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                .thenReturn(saved);

            financialTransactionService.createTransaction(
                apartmentId, validRequest, user.getEmail());

            assertThat(apartment.getCurrentBalance())
                .isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("Kwota ujemna (naliczenie) — saldo maleje")
        void shouldDecreaseBalanceForNegativeAmount() {
            FinancialTransactionRequest chargeRequest = new FinancialTransactionRequest(
                "NALICZENIE",
                new BigDecimal("-300.00"),
                "Naliczenie czynszu za kwiecień",
                LocalDate.of(2026, 4, 1)
            );

            FinancialTransaction saved = new FinancialTransaction();
            saved.setId(UUID.randomUUID());
            saved.setApartment(apartment);
            saved.setType(chargeRequest.getType());
            saved.setAmount(chargeRequest.getAmount());
            saved.setDescription(chargeRequest.getDescription());
            saved.setTransactionDate(chargeRequest.getTransactionDate());
            saved.setRecordedBy(user);

            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
            when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                .thenReturn(saved);

            financialTransactionService.createTransaction(
                apartmentId, chargeRequest, user.getEmail());

            assertThat(apartment.getCurrentBalance())
                .isEqualByComparingTo("-100.00");
        }
    }

    // =======================================================
    // GET TRANSACTIONS FOR APARTMENT
    // =======================================================

    @Nested
    @DisplayName("getTransactionsForApartment()")
    class GetTransactionsTests {

        @Test
        @DisplayName("Istniejący lokal — zwraca saldo i listę transakcji")
        void shouldReturnBalanceAndTransactions() {
            FinancialTransaction transaction = new FinancialTransaction();
            transaction.setId(UUID.randomUUID());
            transaction.setApartment(apartment);
            transaction.setType("WPLATA");
            transaction.setAmount(new BigDecimal("500.00"));
            transaction.setDescription("Wpłata czynszu");
            transaction.setTransactionDate(LocalDate.of(2026, 4, 15));
            transaction.setRecordedBy(user);

            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(financialTransactionRepository
                .findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                .thenReturn(List.of(transaction));

            ApartmentTransactionsResponse response =
                financialTransactionService.getTransactionsForApartment(apartmentId);

            assertThat(response.getCurrentBalance())
                .isEqualByComparingTo("200.00");
            assertThat(response.getTransactions()).hasSize(1);
            assertThat(response.getTransactions().get(0).getType())
                .isEqualTo("WPLATA");
        }

        @Test
        @DisplayName("Nieistniejący lokal — rzuca NotFoundException")
        void shouldThrowNotFoundForNonExistentApartment() {
            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                financialTransactionService.getTransactionsForApartment(apartmentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(apartmentId.toString());
        }

        @Test
        @DisplayName("Lokal bez transakcji — zwraca saldo i pustą listę")
        void shouldReturnEmptyListWhenNoTransactions() {
            when(apartmentRepository.findById(apartmentId))
                .thenReturn(Optional.of(apartment));
            when(financialTransactionRepository
                .findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                .thenReturn(List.of());

            ApartmentTransactionsResponse response =
                financialTransactionService.getTransactionsForApartment(apartmentId);

            assertThat(response.getCurrentBalance())
                .isEqualByComparingTo("200.00");
            assertThat(response.getTransactions()).isEmpty();
        }
    }
}

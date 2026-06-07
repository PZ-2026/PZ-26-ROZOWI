package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.ur.blokur.dto.ApartmentBalanceResponse;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApartmentBalanceService — zestawienie sald i zaległości lokali")
class ApartmentBalanceServiceTest {

    @Mock private ApartmentRepository apartmentRepository;
    @Mock private FinancialTransactionRepository transactionRepository;

    @InjectMocks private ApartmentBalanceService service;

    private UUID apt1Id;
    private UUID apt2Id;
    private Apartment apt1;
    private Apartment apt2;

    @BeforeEach
    void setUp() {
        apt1Id = UUID.randomUUID();
        apt2Id = UUID.randomUUID();

        Building building = new Building();
        building.setAddress("ul. Testowa 1");

        Staircase staircase = new Staircase();
        staircase.setBuilding(building);

        apt1 = new Apartment();
        apt1.setId(apt1Id);
        apt1.setNumber("1");
        apt1.setStaircase(staircase);
        apt1.setCurrentBalance(new BigDecimal("-200.00"));

        apt2 = new Apartment();
        apt2.setId(apt2Id);
        apt2.setNumber("2");
        apt2.setStaircase(staircase);
        apt2.setCurrentBalance(new BigDecimal("-500.00"));
    }

    @Nested
    @DisplayName("getBalances — pobieranie wszystkich lokali")
    class GetBalancesAll {

        @Test
        @DisplayName("Brak filtrów — zwraca wszystkie lokale posortowane malejąco po zaległości")
        void shouldReturnAllApartmentsSortedByDebtDesc() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1, apt2));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getBalance()).isEqualByComparingTo("-500.00");
            assertThat(result.get(1).getBalance()).isEqualByComparingTo("-200.00");
        }

        @Test
        @DisplayName("Brak lokali — zwraca pustą listę")
        void shouldReturnEmptyListWhenNoApartments() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Adres budowany jako: adres budynku + ' m. ' + numer lokalu")
        void shouldBuildAddressCorrectly() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result.get(0).getAddress()).isEqualTo("ul. Testowa 1 m. 1");
        }

        @Test
        @DisplayName("Lokal z null balance — traktuje saldo jako 0.00")
        void shouldTreatNullBalanceAsZero() {
            apt1.setCurrentBalance(null);
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result.get(0).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getBalances — filtrowanie po nieruchomości")
    class GetBalancesByProperty {

        @Test
        @DisplayName("Podane propertyId — używa findAllByPropertyId")
        void shouldFilterByPropertyId() {
            UUID propertyId = UUID.randomUUID();
            when(apartmentRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result =
                    service.getBalances(propertyId, null, null, true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApartmentId()).isEqualTo(apt1Id);
        }
    }

    @Nested
    @DisplayName("getBalances — filtrowanie po kwocie zaległości (minDebt)")
    class GetBalancesMinDebt {

        @Test
        @DisplayName("minDebt=300 — zwraca tylko lokale z zaległością ≥ 300 PLN")
        void shouldFilterByMinDebt() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1, apt2));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result =
                    service.getBalances(null, new BigDecimal("300"), null, true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBalance()).isEqualByComparingTo("-500.00");
        }

        @Test
        @DisplayName("minDebt=100 — zwraca lokale z saldem ≤ -100")
        void shouldIncludeAllApartmentsWhenMinDebtSmall() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1, apt2));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result =
                    service.getBalances(null, new BigDecimal("100"), null, true);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Lokal z saldem dodatnim — nie przechodzi filtra minDebt")
        void shouldExcludePositiveBalanceWhenMinDebtSet() {
            apt1.setCurrentBalance(new BigDecimal("100.00"));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result =
                    service.getBalances(null, new BigDecimal("1"), null, true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBalances — filtrowanie po dniach zalegania (minDaysOverdue)")
    class GetBalancesMinDaysOverdue {

        @Test
        @DisplayName("Lokal bez wpłaty — daysOverdue jest null, nie przechodzi filtra")
        void shouldExcludeApartmentWithNoPaymentWhenFilterSet() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, 30L, true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Lokal z ostatnią wpłatą 60 dni temu — przechodzi filtr minDaysOverdue=30")
        void shouldIncludeApartmentWithSufficientDaysOverdue() {
            LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.<Object[]>of(new Object[] {apt1Id, sixtyDaysAgo}));

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, 30L, true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDaysOverdue()).isGreaterThanOrEqualTo(60L);
        }

        @Test
        @DisplayName("Lokal z ostatnią wpłatą 5 dni temu — nie przechodzi filtra minDaysOverdue=30")
        void shouldExcludeApartmentWithTooFewDaysOverdue() {
            LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.<Object[]>of(new Object[] {apt1Id, fiveDaysAgo}));

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, 30L, true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBalances — sortowanie")
    class GetBalancesSorting {

        @Test
        @DisplayName(
                "sortDesc=false — sortuje rosnąco (najmniejsza zaległość na pierwszym miejscu)")
        void shouldSortAscending() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt2, apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, false);

            assertThat(result.get(0).getBalance()).isEqualByComparingTo("-200.00");
            assertThat(result.get(1).getBalance()).isEqualByComparingTo("-500.00");
        }

        @Test
        @DisplayName(
                "sortDesc=true — sortuje malejąco po zaległości (największa zaległość na pierwszym"
                        + " miejscu)")
        void shouldSortDescending() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1, apt2));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result.get(0).getBalance()).isEqualByComparingTo("-500.00");
        }
    }

    @Nested
    @DisplayName("getBalances — data ostatniej wpłaty i dni zalegania")
    class GetBalancesPaymentInfo {

        @Test
        @DisplayName("Lokal z wpłatą — lastPaymentDate i daysOverdue są wypełnione")
        void shouldReturnLastPaymentDateAndDaysOverdue() {
            LocalDate paymentDate = LocalDate.now().minusDays(45);
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.<Object[]>of(new Object[] {apt1Id, paymentDate}));

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result.get(0).getLastPaymentDate()).isEqualTo(paymentDate);
            assertThat(result.get(0).getDaysOverdue()).isGreaterThanOrEqualTo(45L);
        }

        @Test
        @DisplayName("Lokal bez żadnej wpłaty — lastPaymentDate i daysOverdue są null")
        void shouldReturnNullWhenNoPayment() {
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apt1));
            when(transactionRepository.findLastPaymentDatesByApartmentIds(anyList()))
                    .thenReturn(List.of());

            List<ApartmentBalanceResponse> result = service.getBalances(null, null, null, true);

            assertThat(result.get(0).getLastPaymentDate()).isNull();
            assertThat(result.get(0).getDaysOverdue()).isNull();
        }
    }
}

package pl.edu.ur.blokur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
import pl.edu.ur.blokur.dto.AnnualSettlementDistributionRequest;
import pl.edu.ur.blokur.dto.DocumentDistributionResult;
import pl.edu.ur.blokur.dto.RateChangeDistributionRequest;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.Building;
import pl.edu.ur.blokur.models.FinancialTransaction;
import pl.edu.ur.blokur.models.Property;
import pl.edu.ur.blokur.models.Staircase;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.models.UserApartment;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;
import pl.edu.ur.blokur.repository.PropertyRepository;
import pl.edu.ur.blokur.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentDistributionService — masowe generowanie i dystrybucja dokumentów PDF")
class DocumentDistributionServiceTest {

    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private DocumentService documentService;
    @Mock private PushNotificationService pushNotificationService;
    @Mock private UserRepository userRepository;
    @Mock private ApartmentRepository apartmentRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private FinancialTransactionRepository financialTransactionRepository;

    @InjectMocks private DocumentDistributionService documentDistributionService;

    private static final String MANAGER_EMAIL = "zarzadca@blokur.pl";
    private static final byte[] DUMMY_PDF = new byte[]{1, 2, 3};

    private User manager;
    private User resident;
    private Apartment apartment;
    private Building building;
    private Staircase staircase;
    private Property property;
    private UUID buildingId;
    private UUID apartmentId;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();

        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setEmail(MANAGER_EMAIL);
        manager.setRole("ZARZADCA");

        resident = new User();
        resident.setId(UUID.randomUUID());
        resident.setEmail("mieszkaniec@blokur.pl");
        resident.setRole("MIESZKANIEC");
        resident.setDeleted(false);

        property = new Property();
        property.setId(UUID.randomUUID());
        property.setName("Wspólnota Testowa");

        building = new Building();
        building.setId(buildingId);
        building.setAddress("ul. Testowa 1");
        building.setProperty(property);

        staircase = new Staircase();
        staircase.setId(UUID.randomUUID());
        staircase.setBuilding(building);

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setNumber("1A");
        apartment.setStaircase(staircase);

        UserApartment ua = new UserApartment();
        ua.setUser(resident);
        ua.setApartment(apartment);
        apartment.getUserApartments().add(ua);
    }

    // =========================================================
    // distributeRateChange tests
    // =========================================================

    @Nested
    @DisplayName("distributeRateChange — zawiadomienie o zmianie stawek")
    class DistributeRateChangeTests {

        private RateChangeDistributionRequest makeRequest(String scope, String targetId) {
            RateChangeDistributionRequest req = new RateChangeDistributionRequest();
            req.setSubject("Zmiana stawek");
            req.setBody("Informujemy o zmianie stawek opłat.");
            req.setEffectiveDate("2026-07-01");
            req.setScope(scope);
            req.setTargetId(targetId);
            return req;
        }

        @Test
        @DisplayName("Zwraca wynik z liczbą dokumentów = liczbie aktywnych mieszkańców (scope ALL)")
        void shouldGenerateDocumentForEachActiveResident() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeRateChange(
                            makeRequest("ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
            assertThat(result.getRecipientsNotified()).isEqualTo(1);
            verify(documentService, times(1)).storeGeneratedDocument(
                    eq("ZAWIADOMIENIE_STAWKI"), anyString(), eq(DUMMY_PDF), eq(resident),
                    eq(apartment), eq(null), eq(null));
        }

        @Test
        @DisplayName("Pomija usuniętych (deleted) mieszkańców")
        void shouldSkipDeletedResidents() {
            resident.setDeleted(true);

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeRateChange(
                            makeRequest("ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(0);
            verify(documentService, never()).storeGeneratedDocument(
                    any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Nie wysyła powiadomień gdy brak odbiorców")
        void shouldNotSendPushWhenNoRecipients() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(Collections.emptyList());
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            documentDistributionService.distributeRateChange(
                    makeRequest("ALL", null), MANAGER_EMAIL);

            verify(pushNotificationService, never()).sendToUsers(
                    anyList(), anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Wysyła powiadomienia PUSH gdy są odbiorcy")
        void shouldSendPushWhenRecipientsExist() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            documentDistributionService.distributeRateChange(
                    makeRequest("ALL", null), MANAGER_EMAIL);

            verify(pushNotificationService, times(1)).sendToUsers(
                    anyList(), anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Rzuca NotFoundException gdy zarządca nie istnieje")
        void shouldThrowWhenManagerNotFound() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    documentDistributionService.distributeRateChange(
                            makeRequest("ALL", null), MANAGER_EMAIL))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Filtruje lokale wg budynku gdy scope=BUILDING")
        void shouldFilterByBuildingWhenScopeIsBuilding() {
            Apartment otherApartment = new Apartment();
            otherApartment.setId(UUID.randomUUID());
            otherApartment.setNumber("2B");
            Building otherBuilding = new Building();
            otherBuilding.setId(UUID.randomUUID());
            otherBuilding.setAddress("ul. Inna 2");
            Staircase otherStaircase = new Staircase();
            otherStaircase.setBuilding(otherBuilding);
            otherApartment.setStaircase(otherStaircase);

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment, otherApartment));
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeRateChange(
                            makeRequest("BUILDING", buildingId.toString()), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
        }

        @Test
        @DisplayName("Filtruje do jednego lokalu gdy scope=APARTMENT")
        void shouldFilterToSingleApartmentWhenScopeIsApartment() {
            Apartment otherApartment = new Apartment();
            otherApartment.setId(UUID.randomUUID());
            otherApartment.setNumber("2B");
            otherApartment.setStaircase(staircase);

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment, otherApartment));
            when(pdfGeneratorService.generateRateChangeNotification(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeRateChange(
                            makeRequest("APARTMENT", apartmentId.toString()), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
        }
    }

    // =========================================================
    // distributeAnnualSettlement tests
    // =========================================================

    @Nested
    @DisplayName("distributeAnnualSettlement — roczne rozliczenia")
    class DistributeAnnualSettlementTests {

        private AnnualSettlementDistributionRequest makeRequest(int year, String scope, String targetId) {
            AnnualSettlementDistributionRequest req = new AnnualSettlementDistributionRequest();
            req.setYear(year);
            req.setNote("Uwagi do rozliczenia");
            req.setScope(scope);
            req.setTargetId(targetId);
            return req;
        }

        @Test
        @DisplayName("Generuje rozliczenie z transakcjami w danym roku (scope ALL)")
        void shouldGenerateSettlementWithYearTransactions() {
            FinancialTransaction t1 = new FinancialTransaction();
            t1.setTransactionDate(LocalDate.of(2025, 3, 15));
            t1.setType("WPLATA");
            t1.setDescription("Wpłata za marzec");
            t1.setAmount(new BigDecimal("500.00"));

            FinancialTransaction t2 = new FinancialTransaction();
            t2.setTransactionDate(LocalDate.of(2025, 6, 10));
            t2.setType("NALICZENIE");
            t2.setDescription("Naliczenie za czerwiec");
            t2.setAmount(new BigDecimal("-300.00"));

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                    .thenReturn(List.of(t1, t2));
            when(pdfGeneratorService.generateAnnualSettlement(any())).thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
            assertThat(result.getRecipientsNotified()).isEqualTo(1);
        }

        @Test
        @DisplayName("Oblicza openingBalance z transakcji przed rokiem rozliczeniowym")
        void shouldCalculateOpeningBalanceFromPreviousTransactions() {
            FinancialTransaction prev = new FinancialTransaction();
            prev.setTransactionDate(LocalDate.of(2024, 12, 31));
            prev.setType("WPLATA");
            prev.setDescription("Poprzedni rok");
            prev.setAmount(new BigDecimal("200.00"));

            FinancialTransaction curr = new FinancialTransaction();
            curr.setTransactionDate(LocalDate.of(2025, 5, 1));
            curr.setType("NALICZENIE");
            curr.setDescription("Rok bieżący");
            curr.setAmount(new BigDecimal("-100.00"));

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                    .thenReturn(List.of(prev, curr));
            when(pdfGeneratorService.generateAnnualSettlement(any())).thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
        }

        @Test
        @DisplayName("Zwraca zero dokumentów gdy brak lokali")
        void shouldReturnZeroWhenNoApartments() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(Collections.emptyList());

            DocumentDistributionResult result =
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(0);
            verify(pdfGeneratorService, never()).generateAnnualSettlement(any());
        }

        @Test
        @DisplayName("Pomija lokalów bez przypisanych mieszkańców")
        void shouldSkipApartmentsWithNoResidents() {
            apartment.getUserApartments().clear();

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                    .thenReturn(Collections.emptyList());
            when(pdfGeneratorService.generateAnnualSettlement(any())).thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "ALL", null), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(0);
        }

        @Test
        @DisplayName("Rzuca NotFoundException gdy zarządca nie istnieje")
        void shouldThrowWhenManagerNotFound() {
            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "ALL", null), MANAGER_EMAIL))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Wysyła powiadomienia PUSH po wygenerowaniu rozliczeń")
        void shouldSendPushAfterGeneratingSettlements() {
            FinancialTransaction t = new FinancialTransaction();
            t.setTransactionDate(LocalDate.of(2025, 1, 15));
            t.setType("WPLATA");
            t.setDescription("Test");
            t.setAmount(new BigDecimal("100.00"));

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                    .thenReturn(List.of(t));
            when(pdfGeneratorService.generateAnnualSettlement(any())).thenReturn(DUMMY_PDF);

            documentDistributionService.distributeAnnualSettlement(
                    makeRequest(2025, "ALL", null), MANAGER_EMAIL);

            verify(pushNotificationService, times(1)).sendToUsers(
                    anyList(), anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Filtruje do jednego lokalu gdy scope=APARTMENT")
        void shouldFilterToSingleApartmentScope() {
            Apartment otherApartment = new Apartment();
            otherApartment.setId(UUID.randomUUID());
            otherApartment.setNumber("2B");
            otherApartment.setStaircase(staircase);

            when(userRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
            when(propertyRepository.findAll()).thenReturn(List.of(property));
            when(apartmentRepository.findAllWithBuilding()).thenReturn(List.of(apartment, otherApartment));
            when(financialTransactionRepository.findByApartmentIdOrderByTransactionDateDesc(apartmentId))
                    .thenReturn(Collections.emptyList());
            when(pdfGeneratorService.generateAnnualSettlement(any())).thenReturn(DUMMY_PDF);

            DocumentDistributionResult result =
                    documentDistributionService.distributeAnnualSettlement(
                            makeRequest(2025, "APARTMENT", apartmentId.toString()), MANAGER_EMAIL);

            assertThat(result.getDocumentsGenerated()).isEqualTo(1);
        }
    }
}

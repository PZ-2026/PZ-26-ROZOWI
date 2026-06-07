package pl.edu.ur.blokur.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.ur.blokur.dto.AnnualSettlementDistributionRequest;
import pl.edu.ur.blokur.dto.DocumentDistributionResult;
import pl.edu.ur.blokur.dto.RateChangeDistributionRequest;
import pl.edu.ur.blokur.exception.NotFoundException;
import pl.edu.ur.blokur.models.Apartment;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.pdflib.template.data.AnnualSettlementData;
import pl.edu.ur.blokur.pdflib.template.data.AnnualSettlementRow;
import pl.edu.ur.blokur.repository.ApartmentRepository;
import pl.edu.ur.blokur.repository.BuildingRepository;
import pl.edu.ur.blokur.repository.FinancialTransactionRepository;
import pl.edu.ur.blokur.repository.PropertyRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis odpowiedzialny za masowe generowanie i dystrybucję dokumentów PDF do mieszkańców.
 * Obsługuje zawiadomienia o zmianie stawek oraz roczne rozliczenia kosztów lokali.
 */
@Service
public class DocumentDistributionService {

    private final PdfGeneratorService pdfGeneratorService;
    private final DocumentService documentService;
    private final PushNotificationService pushNotificationService;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final PropertyRepository propertyRepository;
    private final FinancialTransactionRepository financialTransactionRepository;

    /**
     * Tworzy serwis dystrybucji dokumentów i wstrzykuje wymagane zależności.
     *
     * @param pdfGeneratorService serwis generowania plików PDF
     * @param documentService serwis zapisu i przechowywania dokumentów
     * @param pushNotificationService serwis wysyłania powiadomień PUSH
     * @param userRepository repozytorium użytkowników
     * @param apartmentRepository repozytorium lokali
     * @param buildingRepository repozytorium budynków
     * @param propertyRepository repozytorium nieruchomości
     * @param financialTransactionRepository repozytorium transakcji finansowych
     */
    public DocumentDistributionService(
            PdfGeneratorService pdfGeneratorService,
            DocumentService documentService,
            PushNotificationService pushNotificationService,
            UserRepository userRepository,
            ApartmentRepository apartmentRepository,
            BuildingRepository buildingRepository,
            PropertyRepository propertyRepository,
            FinancialTransactionRepository financialTransactionRepository) {
        this.pdfGeneratorService = pdfGeneratorService;
        this.documentService = documentService;
        this.pushNotificationService = pushNotificationService;
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
        this.propertyRepository = propertyRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    /**
     * Generuje zawiadomienie o zmianie stawek i dystrybuuje je do wybranych mieszkańców. Każdy
     * odbiorca dostaje własny wpis dokumentu i powiadomienie PUSH.
     *
     * @param request dane zawiadomienia (treść, data, zakres odbiorców)
     * @param managerEmail email zalogowanego zarządcy
     * @return wynik dystrybucji (liczba dokumentów, liczba powiadomień)
     */
    @Transactional
    public DocumentDistributionResult distributeRateChange(
            RateChangeDistributionRequest request, String managerEmail) {
        userRepository
                .findByEmail(managerEmail)
                .orElseThrow(() -> new NotFoundException("Zarządca nie istnieje"));

        String communityName = resolveCommunityName(request.getScope(), request.getTargetId());
        byte[] pdfBytes =
                pdfGeneratorService.generateRateChangeNotification(
                        request.getSubject(),
                        request.getBody(),
                        request.getEffectiveDate(),
                        communityName);

        List<Apartment> targetApartments =
                resolveTargetApartments(request.getScope(), request.getTargetId());

        int docCount = 0;
        List<UUID> recipientIds = new ArrayList<>();

        for (Apartment apt : targetApartments) {
            for (var ua : apt.getUserApartments()) {
                var resident = ua.getUser();
                if (resident == null || !isActive(resident)) continue;

                documentService.storeGeneratedDocument(
                        "ZAWIADOMIENIE_STAWKI",
                        "Zawiadomienie o zmianie stawek — " + request.getEffectiveDate(),
                        pdfBytes,
                        resident,
                        apt,
                        null,
                        null);

                docCount++;
                recipientIds.add(resident.getId());
            }
        }

        if (!recipientIds.isEmpty()) {
            pushNotificationService.sendToUsers(
                    recipientIds,
                    PushNotificationService.EVENT_NOWY_DOKUMENT,
                    "Nowy dokument",
                    "Zarządca udostępnił nowy dokument: Zawiadomienie o zmianie stawek",
                    Map.of("type", "ZAWIADOMIENIE_STAWKI"));
        }

        return new DocumentDistributionResult(
                docCount,
                recipientIds.size(),
                "Zawiadomienie wygenerowane i wysłane do " + docCount + " lokali.");
    }

    /**
     * Generuje roczne rozliczenia kosztów dla wybranych lokali i dystrybuuje je do mieszkańców.
     *
     * @param request dane rozliczenia (rok, uwagi, zakres odbiorców)
     * @param managerEmail email zalogowanego zarządcy
     * @return wynik dystrybucji (liczba dokumentów, liczba powiadomień)
     */
    @Transactional
    public DocumentDistributionResult distributeAnnualSettlement(
            AnnualSettlementDistributionRequest request, String managerEmail) {
        userRepository
                .findByEmail(managerEmail)
                .orElseThrow(() -> new NotFoundException("Zarządca nie istnieje"));

        int year = request.getYear();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<Apartment> targetApartments =
                resolveTargetApartments(request.getScope(), request.getTargetId());

        String communityName = resolveCommunityName(request.getScope(), request.getTargetId());

        int docCount = 0;
        List<UUID> recipientIds = new ArrayList<>();

        for (Apartment apt : targetApartments) {
            var allTransactions =
                    financialTransactionRepository
                            .findByApartmentIdOrderByTransactionDateDesc(apt.getId());

            BigDecimal openingBalance =
                    allTransactions.stream()
                            .filter(t -> t.getTransactionDate().isBefore(yearStart))
                            .map(t -> t.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<AnnualSettlementRow> rows =
                    allTransactions.stream()
                            .filter(
                                    t ->
                                            !t.getTransactionDate().isBefore(yearStart)
                                                    && !t.getTransactionDate().isAfter(yearEnd))
                            .sorted(
                                    (a, b) ->
                                            a.getTransactionDate()
                                                    .compareTo(b.getTransactionDate()))
                            .map(
                                    t ->
                                            new AnnualSettlementRow(
                                                    t.getTransactionDate(),
                                                    t.getType(),
                                                    t.getDescription(),
                                                    t.getAmount()))
                            .collect(Collectors.toList());

            BigDecimal yearSum =
                    rows.stream()
                            .map(AnnualSettlementRow::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal closingBalance = openingBalance.add(yearSum);

            String address = buildApartmentAddress(apt);

            AnnualSettlementData data =
                    new AnnualSettlementData(
                            address,
                            year,
                            openingBalance,
                            closingBalance,
                            rows,
                            request.getNote(),
                            communityName);

            byte[] pdfBytes = pdfGeneratorService.generateAnnualSettlement(data);

            for (var ua : apt.getUserApartments()) {
                var resident = ua.getUser();
                if (resident == null || !isActive(resident)) continue;

                documentService.storeGeneratedDocument(
                        "ROZLICZENIE_ROCZNE",
                        "Rozliczenie roczne " + year + " — " + address,
                        pdfBytes,
                        resident,
                        apt,
                        null,
                        null);

                docCount++;
                recipientIds.add(resident.getId());
            }
        }

        if (!recipientIds.isEmpty()) {
            pushNotificationService.sendToUsers(
                    recipientIds,
                    PushNotificationService.EVENT_NOWY_DOKUMENT,
                    "Nowy dokument",
                    "Zarządca udostępnił nowy dokument: Rozliczenie roczne " + year,
                    Map.of("type", "ROZLICZENIE_ROCZNE", "year", String.valueOf(year)));
        }

        return new DocumentDistributionResult(
                docCount,
                recipientIds.size(),
                "Rozliczenie za " + year + " wygenerowane dla " + docCount + " lokali.");
    }

    private List<Apartment> resolveTargetApartments(String scope, String targetId) {
        if (scope == null || "ALL".equalsIgnoreCase(scope)) {
            return apartmentRepository.findAllWithBuilding();
        }
        if ("BUILDING".equalsIgnoreCase(scope) && targetId != null) {
            UUID buildingId = UUID.fromString(targetId);
            return apartmentRepository.findAllWithBuilding().stream()
                    .filter(a -> a.getStaircase().getBuilding().getId().equals(buildingId))
                    .collect(Collectors.toList());
        }
        if ("APARTMENT".equalsIgnoreCase(scope) && targetId != null) {
            UUID apartmentId = UUID.fromString(targetId);
            return apartmentRepository.findAllWithBuilding().stream()
                    .filter(a -> a.getId().equals(apartmentId))
                    .collect(Collectors.toList());
        }
        return apartmentRepository.findAllWithBuilding();
    }

    private String resolveCommunityName(String scope, String targetId) {
        if ("BUILDING".equalsIgnoreCase(scope) && targetId != null) {
            return buildingRepository
                    .findById(UUID.fromString(targetId))
                    .map(b -> b.getProperty() != null ? b.getProperty().getName() : "BLOKUR")
                    .orElse("BLOKUR");
        }
        var all = propertyRepository.findAll();
        if (all.size() == 1) return all.get(0).getName();
        return "BLOKUR";
    }

    private static String buildApartmentAddress(Apartment apt) {
        return "%s lok. %s"
                .formatted(apt.getStaircase().getBuilding().getAddress(), apt.getNumber());
    }

    private static boolean isActive(User user) {
        return !user.isDeleted();
    }
}

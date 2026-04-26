package pl.edu.ur.blokur.services

import pl.edu.ur.blokur.dtos.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serwis finansów — aktualnie mockowe dane (do przyszłej zamiany na Retrofit).
 */
@Singleton
class FinancesService @Inject constructor() {

    private val admin = AppUserDto("Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")

    suspend fun getBalance(): ApartmentBalanceDto = ApartmentBalanceDto(
        apartmentNumber = "45",
        currentBalance = 150.25,
        currency = "PLN",
        lastTransactionDate = "2026-03-10",
        status = BalanceStatus.NADPLATA,
        totalPaid = 2700.00,
        totalCharged = 2549.75
    )

    suspend fun getTransactions(): List<TransactionDto> = listOf(
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za marzec 2026", "2026-03-10", admin),
        TransactionDto(TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za marzec 2026", "2026-03-01", admin),
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za luty 2026", "2026-02-08", admin),
        TransactionDto(TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za luty 2026", "2026-02-01", admin),
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za styczeń 2026", "2026-01-07", admin),
        TransactionDto(TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za styczeń 2026", "2026-01-01", admin),
        TransactionDto(TransactionType.KOREKTA, 30.50, "Korekta rozliczenia wody za II półrocze 2025", "2026-01-15", admin),
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za grudzień 2025", "2025-12-06", admin),
        TransactionDto(TransactionType.NALICZENIE, -455.75, "Naliczenie czynszu za grudzień 2025 (podwyżka ogrzewania)", "2025-12-01", admin),
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za listopad 2025", "2025-11-07", admin),
        TransactionDto(TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za listopad 2025", "2025-11-01", admin),
        TransactionDto(TransactionType.WPLATA, 450.00, "Przelew – czynsz za październik 2025", "2025-10-09", admin),
        TransactionDto(TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za październik 2025", "2025-10-01", admin)
    )

    suspend fun getDocuments(): List<DocumentDto> = listOf(
        DocumentDto(DocumentType.ROZLICZENIE, "Rozliczenie wody – I półrocze 2026", "https://cdn.blokur.pl/docs/apt45/rozliczenie-woda-2026-h1.pdf", 2026, "2026-03-15T08:00:00Z"),
        DocumentDto(DocumentType.NALICZENIE, "Naliczenie opłat – marzec 2026", "https://cdn.blokur.pl/docs/apt45/naliczenie-2026-03.pdf", 2026, "2026-03-01T07:00:00Z"),
        DocumentDto(DocumentType.ZAWIADOMIENIE, "Zawiadomienie o zmianie stawki czynszu od 01.04.2026", "https://cdn.blokur.pl/docs/zawiadomienie-stawka.pdf", 2026, "2026-02-20T10:30:00Z"),
        DocumentDto(DocumentType.ROZLICZENIE, "Rozliczenie mediów – II półrocze 2025", "https://cdn.blokur.pl/docs/apt45/rozliczenie-media-2025-h2.pdf", 2025, "2026-01-10T09:00:00Z"),
        DocumentDto(DocumentType.FAKTURA, "Faktura VAT – usługi konserwacyjne XII 2025", "https://cdn.blokur.pl/docs/faktura-konserwacja-2025-12.pdf", 2025, "2025-12-20T14:00:00Z"),
        DocumentDto(DocumentType.ROZLICZENIE, "Rozliczenie wody – I półrocze 2025", "https://cdn.blokur.pl/docs/apt45/rozliczenie-woda-2025-h1.pdf", 2025, "2025-07-15T08:00:00Z"),
        DocumentDto(DocumentType.INNE, "Regulamin korzystania z części wspólnych 2025", "https://cdn.blokur.pl/docs/regulamin-2025.pdf", 2025, "2025-01-05T10:00:00Z")
    )
}

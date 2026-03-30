package pl.edu.ur.blokur.infrastructure.mock

import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.ApartmentBalance
import pl.edu.ur.blokur.domain.model.BalanceStatus
import pl.edu.ur.blokur.domain.model.DocumentType
import pl.edu.ur.blokur.domain.model.FinancialDocument
import pl.edu.ur.blokur.domain.model.FinancialTransaction
import pl.edu.ur.blokur.domain.model.TransactionType
import pl.edu.ur.blokur.domain.repository.FinancesRepository
import javax.inject.Inject

internal class MockFinancesRepository @Inject constructor() : FinancesRepository {

    private val admin = AppUser(301, "Anna", "Zarządca", "anna.z@blokur.pl", "ADMINISTRATOR")

    override suspend fun getBalance(): ApartmentBalance = ApartmentBalance(
        apartmentId = 45,
        apartmentNumber = "45",
        currentBalance = 150.25,
        currency = "PLN",
        lastTransactionDate = "2026-03-10",
        status = BalanceStatus.NADPLATA,
        totalPaid = 2700.00,
        totalCharged = 2549.75
    )

    override suspend fun getTransactions(): List<FinancialTransaction> = listOf(
        FinancialTransaction(17, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za marzec 2026", "2026-03-10", admin),
        FinancialTransaction(16, 45, TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za marzec 2026", "2026-03-01", admin),
        FinancialTransaction(15, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za luty 2026", "2026-02-08", admin),
        FinancialTransaction(14, 45, TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za luty 2026", "2026-02-01", admin),
        FinancialTransaction(13, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za styczeń 2026", "2026-01-07", admin),
        FinancialTransaction(12, 45, TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za styczeń 2026", "2026-01-01", admin),
        FinancialTransaction(11, 45, TransactionType.KOREKTA, 30.50, "Korekta rozliczenia wody za II półrocze 2025", "2026-01-15", admin),
        FinancialTransaction(10, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za grudzień 2025", "2025-12-06", admin),
        FinancialTransaction(9, 45, TransactionType.NALICZENIE, -455.75, "Naliczenie czynszu za grudzień 2025 (podwyżka ogrzewania)", "2025-12-01", admin),
        FinancialTransaction(8, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za listopad 2025", "2025-11-07", admin),
        FinancialTransaction(7, 45, TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za listopad 2025", "2025-11-01", admin),
        FinancialTransaction(6, 45, TransactionType.WPLATA, 450.00, "Przelew – czynsz za październik 2025", "2025-10-09", admin),
        FinancialTransaction(5, 45, TransactionType.NALICZENIE, -430.00, "Naliczenie czynszu za październik 2025", "2025-10-01", admin)
    )

    override suspend fun getDocuments(): List<FinancialDocument> = listOf(
        FinancialDocument(1, DocumentType.ROZLICZENIE, "Rozliczenie wody – I półrocze 2026", "https://cdn.blokur.pl/docs/apt45/rozliczenie-woda-2026-h1.pdf", 2026, "2026-03-15T08:00:00Z"),
        FinancialDocument(2, DocumentType.NALICZENIE, "Naliczenie opłat – marzec 2026", "https://cdn.blokur.pl/docs/apt45/naliczenie-2026-03.pdf", 2026, "2026-03-01T07:00:00Z"),
        FinancialDocument(3, DocumentType.ZAWIADOMIENIE, "Zawiadomienie o zmianie stawki czynszu od 01.04.2026", "https://cdn.blokur.pl/docs/zawiadomienie-stawka.pdf", 2026, "2026-02-20T10:30:00Z"),
        FinancialDocument(4, DocumentType.ROZLICZENIE, "Rozliczenie mediów – II półrocze 2025", "https://cdn.blokur.pl/docs/apt45/rozliczenie-media-2025-h2.pdf", 2025, "2026-01-10T09:00:00Z"),
        FinancialDocument(5, DocumentType.FAKTURA, "Faktura VAT – usługi konserwacyjne XII 2025", "https://cdn.blokur.pl/docs/faktura-konserwacja-2025-12.pdf", 2025, "2025-12-20T14:00:00Z"),
        FinancialDocument(6, DocumentType.ROZLICZENIE, "Rozliczenie wody – I półrocze 2025", "https://cdn.blokur.pl/docs/apt45/rozliczenie-woda-2025-h1.pdf", 2025, "2025-07-15T08:00:00Z"),
        FinancialDocument(7, DocumentType.INNE, "Regulamin korzystania z części wspólnych 2025", "https://cdn.blokur.pl/docs/regulamin-2025.pdf", 2025, "2025-01-05T10:00:00Z")
    )
}

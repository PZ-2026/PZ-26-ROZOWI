package pl.edu.ur.blokur.domain.model

enum class TransactionType { WPLATA, NALICZENIE, KOREKTA }

enum class DocumentType { NALICZENIE, ROZLICZENIE, ZAWIADOMIENIE, FAKTURA, INNE }

enum class BalanceStatus { NADPLATA, ZALEGLOSC, WYZEROWANY }

data class ApartmentBalance(
    val apartmentId: Int,
    val apartmentNumber: String,
    val currentBalance: Double,
    val currency: String,
    val lastTransactionDate: String?,
    val status: BalanceStatus,
    val totalPaid: Double,
    val totalCharged: Double
)

data class FinancialTransaction(
    val id: Int,
    val apartmentId: Int,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val transactionDate: String,
    val recordedBy: AppUser?
)

data class FinancialDocument(
    val id: Int,
    val type: DocumentType,
    val title: String,
    val fileUrl: String,
    val issueYear: Int,
    val createdAt: String
)

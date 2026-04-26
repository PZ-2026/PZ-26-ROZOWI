package pl.edu.ur.blokur.dtos

enum class TransactionType { WPLATA, NALICZENIE, KOREKTA }

enum class DocumentType { NALICZENIE, ROZLICZENIE, ZAWIADOMIENIE, FAKTURA, INNE }

enum class BalanceStatus { NADPLATA, ZALEGLOSC, WYZEROWANY }

data class ApartmentBalanceDto(
    val apartmentNumber: String,
    val currentBalance: Double,
    val currency: String,
    val lastTransactionDate: String?,
    val status: BalanceStatus,
    val totalPaid: Double,
    val totalCharged: Double
)

data class TransactionDto(
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val transactionDate: String,
    val recordedBy: AppUserDto?
)

data class DocumentDto(
    val type: DocumentType,
    val title: String,
    val fileUrl: String,
    val issueYear: Int,
    val createdAt: String
)

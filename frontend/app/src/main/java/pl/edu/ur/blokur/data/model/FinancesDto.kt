package pl.edu.ur.blokur.data.model

enum class TransactionType {
    WPLATA,
    NALICZENIE,
    KOREKTA
}

enum class DocumentType {
    NALICZENIE,
    ROZLICZENIE,
    ZAWIADOMIENIE,
    FAKTURA,
    INNE
}

enum class BalanceStatus {
    NADPLATA,
    ZALEGLOSC,
    WYZEROWANY
}

data class FinancialTransactionDto(
    val id: Int,
    val apartmentId: Int,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val transactionDate: String,
    val recordedById: Int,
    val recordedBy: AppUserDto?
)

data class FinancialDocumentDto(
    val id: Int,
    val type: DocumentType,
    val title: String,
    val fileUrl: String,
    val issueYear: Int,
    val ownerUserId: Int?,
    val apartmentId: Int?,
    val buildingId: Int?,
    val ticketId: Int?,
    val createdAt: String
)

data class ApartmentBalanceDto(
    val apartmentId: Int,
    val apartmentNumber: String,
    val currentBalance: Double,
    val currency: String,
    val lastTransactionDate: String?,
    val status: BalanceStatus,
    val totalPaid: Double,
    val totalCharged: Double
)

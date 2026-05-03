package pl.edu.ur.blokur.dtos

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Legacy enums (używane przez istniejące mocki) ────────────────────────────
enum class TransactionType { WPLATA, NALICZENIE, KOREKTA }
enum class DocumentType { NALICZENIE, ROZLICZENIE, ZAWIADOMIENIE, FAKTURA, INNE }
enum class BalanceStatus { NADPLATA, ZALEGLOSC, WYZEROWANY }

// ─── Legacy mock DTOs (używane przez istniejące widoki) ──────────────────────
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

// ─── Real API DTOs — GET /api/apartments/{id}/transactions ────────────────────

/** Pełna odpowiedź z historią transakcji i saldem. */
data class ApartmentTransactionsDto(
    @SerializedName("currentBalance") val currentBalance: BigDecimal?,
    @SerializedName("transactions") val transactions: List<FinancialTransactionDto>
)

/** Pojedyncza transakcja finansowa. */
data class FinancialTransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("apartmentId") val apartmentId: String,
    @SerializedName("type") val type: String,           // WPLATA | NALICZENIE | KOREKTA
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("description") val description: String,
    @SerializedName("transactionDate") val transactionDate: String,  // "YYYY-MM-DD"
    @SerializedName("recordedByEmail") val recordedByEmail: String?
) {
    val isCredit: Boolean get() = type == "WPLATA" || amount > BigDecimal.ZERO
}

/** POST /api/apartments/{id}/transactions — ciało żądania. */
data class CreateTransactionRequest(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("description") val description: String,
    @SerializedName("transactionDate") val transactionDate: String  // "YYYY-MM-DD"
)

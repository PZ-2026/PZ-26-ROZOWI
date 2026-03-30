package pl.edu.ur.blokur.domain.repository

import pl.edu.ur.blokur.domain.model.ApartmentBalance
import pl.edu.ur.blokur.domain.model.FinancialDocument
import pl.edu.ur.blokur.domain.model.FinancialTransaction

interface FinancesRepository {
    suspend fun getBalance(): ApartmentBalance
    suspend fun getTransactions(): List<FinancialTransaction>
    suspend fun getDocuments(): List<FinancialDocument>
}

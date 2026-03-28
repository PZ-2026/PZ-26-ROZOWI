package pl.edu.ur.blokur.domain.services

interface LoggingService {
    suspend fun LogMessage(message: String)
}
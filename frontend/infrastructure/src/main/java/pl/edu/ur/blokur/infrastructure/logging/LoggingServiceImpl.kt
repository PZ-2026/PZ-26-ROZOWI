package pl.edu.ur.blokur.infrastructure.logging
import pl.edu.ur.blokur.domain.services.LoggingService
import javax.inject.Inject

internal class LoggingServiceImpl @Inject constructor() : LoggingService {
    override suspend fun LogMessage(message: String)  {
        println(message)
    }
}
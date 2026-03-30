package pl.edu.ur.blokur.application.usecase

import pl.edu.ur.blokur.domain.UseCaseNotImplementedException
import javax.inject.Inject

class ChangeTicketStatusUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class DocumentRepairUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
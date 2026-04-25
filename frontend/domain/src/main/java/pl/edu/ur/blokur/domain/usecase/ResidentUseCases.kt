package pl.edu.ur.blokur.domain.usecase

import pl.edu.ur.blokur.domain.UseCaseNotImplementedException
import javax.inject.Inject

class BrowseAnnouncementsArchiveUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class BrowseTicketsUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class CreateServiceTicketUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class BrowseCurrentTicketsUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class DownloadPdfDocumentUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class ParticipateInSurveyVotingUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class VoteInSurveyUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class BrowseVotingResultsUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class BrowseFinancialRecordUseCase @Inject constructor() {
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
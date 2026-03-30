package pl.edu.ur.blokur.application.usecase

import pl.edu.ur.blokur.domain.UseCaseNotImplementedException
import javax.inject.Inject

class LoginUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
class PasswordResetUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class SetNewPasswordUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}

class SendPasswordResetLinkUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        throw UseCaseNotImplementedException(this::class.qualifiedName.toString())
    }
}
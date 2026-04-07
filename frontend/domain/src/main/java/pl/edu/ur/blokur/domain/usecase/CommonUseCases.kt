package pl.edu.ur.blokur.domain.usecase

import pl.edu.ur.blokur.domain.services.LoggingService
import javax.inject.Inject

class TestAUseCase @Inject constructor(
    private val loggingService: LoggingService
) {
    suspend operator fun invoke(msg: String) {
        loggingService.LogMessage(msg)
    }
}

class TestBUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestCUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestDUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}

class TestEUseCase @Inject constructor(

){
    suspend operator fun invoke() {
        //implement any logic
    }
}
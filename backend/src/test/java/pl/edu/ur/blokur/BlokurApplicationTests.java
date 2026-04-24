package pl.edu.ur.blokur;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Testy kontekstu aplikacji Spring Boot. */
@Disabled("Tymczasowo wyłączone - brak skonfigurowanej bazy danych dla testów")
@SpringBootTest
class BlokurApplicationTests {

    @Test
    void contextLoads() {}
}

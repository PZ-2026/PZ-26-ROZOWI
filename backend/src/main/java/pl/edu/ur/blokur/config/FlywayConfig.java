package pl.edu.ur.blokur.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
/**
 * Konfiguracja narzędzia Flyway.
 * Ręcznie inicjuje proces migracji bazy danych przy starcie aplikacji, wymuszając wykonanie skryptów.
 */
@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
        return flyway;
    }
}

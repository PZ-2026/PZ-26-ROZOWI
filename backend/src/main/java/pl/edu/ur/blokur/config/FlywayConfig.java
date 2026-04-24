package pl.edu.ur.blokur.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja narzędzia Flyway. Ręcznie inicjuje proces migracji bazy danych przy starcie
 * aplikacji, wymuszając wykonanie skryptów.
 */
@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String[] locations;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway =
                Flyway.configure()
                        .dataSource(dataSource)
                        .locations(locations)
                        .baselineOnMigrate(true)
                        .load();
        return flyway;
    }
}

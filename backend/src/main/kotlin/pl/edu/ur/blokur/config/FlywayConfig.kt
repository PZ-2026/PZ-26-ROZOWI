package pl.edu.ur.blokur.config

import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig(
    private val dataSource: DataSource,
) {
    @PostConstruct
    fun migrateFlyway() {
        try {
            val flyway =
                Flyway
                    .configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
            flyway.migrate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

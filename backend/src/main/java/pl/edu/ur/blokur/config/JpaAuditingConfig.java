package pl.edu.ur.blokur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Konfiguracja audytu JPA — automatyczne wypełnianie pól createdBy/modifiedBy.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Dostarcza nazwę aktualnie zalogowanego użytkownika do mechanizmu audytu.
     *
     * @return implementacja {@link AuditorAware} oparta na kontekście bezpieczeństwa
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(authentication.getName());
        };
    }
}

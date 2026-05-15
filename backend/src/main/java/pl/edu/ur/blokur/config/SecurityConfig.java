package pl.edu.ur.blokur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.edu.ur.blokur.security.JwtAuthenticationFilter;
import pl.edu.ur.blokur.security.RateLimitFilter;

/**
 * Konfiguracja zabezpieczeń aplikacji Spring Security. Definiuje reguły autoryzacji, filtry JWT
 * oraz enkoder haseł BCrypt.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    /**
     * Konfiguruje łańcuch filtrów bezpieczeństwa HTTP. Sesje są bezstanowe (JWT), CSRF wyłączone
     * dla API REST.
     *
     * @param http obiekt konfiguracji HTTP
     * @return skonfigurowany {@link SecurityFilterChain}
     * @throws Exception w przypadku błędu konfiguracji
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/api/auth/login",
                                                "/api/auth/refresh",
                                                "/api/auth/forgot-password",
                                                "/api/auth/reset-password",
                                                "/api/auth/accept-invitation",
                                                "/api/pdf/**",
                                                "/api/categories",
                                                "/error")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Tworzy enkoder haseł oparty na algorytmie BCrypt z kosztem 12. Używany zarówno do weryfikacji
     * haseł przy logowaniu, jak i do hashowania nowych haseł. Koszt 12 spełnia minimalne wymagania
     * specyfikacji bezpieczeństwa (WNF-03).
     *
     * @return {@link PasswordEncoder} BCrypt z kosztem 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Udostępnia menedżera uwierzytelniania Spring Security.
     *
     * @param config konfiguracja uwierzytelniania
     * @return {@link AuthenticationManager}
     * @throws Exception w przypadku błędu konfiguracji
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}

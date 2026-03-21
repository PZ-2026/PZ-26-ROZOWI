package pl.edu.ur.blokur.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/login", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val zarzadca =
            User
                .builder()
                .username("zarzadca")
                .password(passwordEncoder.encode("haslo123"))
                .roles("ZARZADCA")
                .build()

        val mieszkaniec =
            User
                .builder()
                .username("mieszkaniec")
                .password(passwordEncoder.encode("haslo123"))
                .roles("MIESZKANIEC")
                .build()

        val konserwator =
            User
                .builder()
                .username("konserwator")
                .password(passwordEncoder.encode("haslo123"))
                .roles("KONSERWATOR")
                .build()

        return InMemoryUserDetailsManager(zarzadca, mieszkaniec, konserwator)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager
}

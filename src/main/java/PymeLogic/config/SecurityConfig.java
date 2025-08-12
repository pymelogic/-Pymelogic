package PymeLogic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permitir todo por ahora para testing
            )
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF por simplicidad
            .headers(headers -> headers
                .frameOptions().disable() // Permitir frames para desarrollo
            );
        
        return http.build();
    }
}
package cc.openstrata.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Authorization is performed server-side in the application layer (R-005) using
 * the {@link TenantContext} resolved by {@link AuthInterceptor}. The Spring
 * Security filter chain is left permissive so the offline harness can run
 * without an IdP; the gateway/Auth SPI remain the real enforcement boundary.
 */
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

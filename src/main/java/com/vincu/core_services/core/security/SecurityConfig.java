package com.vincu.core_services.core.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Log4j2
public class SecurityConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authorities);

        String diagnosticOffers = "/api/v1/jobs/{jobId}/diagnostic-offers";
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // Desactivado porque las APIs REST no usan cookies de sesión tradicionales
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, diagnosticOffers).hasRole("TECHNICIAN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs/diagnostic-offers/mine").hasRole("TECHNICIAN")
                        .requestMatchers(HttpMethod.GET, diagnosticOffers).hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PATCH, diagnosticOffers + "/{offerId}/accept").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/{id}").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/jobs/{id}/complete").hasAnyRole("CLIENT", "TECHNICIAN")
                        .requestMatchers("/api/v1/jobs/{id}/review").hasRole("CLIENT")
                        .requestMatchers("/api/v1/client/{id}/client-review").hasRole("TECHNICIAN")
                        // Permite el acceso sin restricciones (sin login) a:
                        // POST /api/v1/users/register/client
                        // POST /api/v1/users/register/technician
                        .requestMatchers(
                                "/api/v1/users/register/**",
                                "/api/v1/auth/login",
                                "/error",
                                "/api/v1/jobs",
                                "/api/v1/jobs/**",
                                "/api/v1/ubigeo/**",
                                "/api/v1/clients/**", "/api/v1/client/**",
                                "/api/v1/technicians/**",
                                "/api/v1/complaints", "/api/v1/complaints/**"// TODO: quitar al migrar a JWT
                        ).permitAll()

                        // Cualquier otra petición futura requerirá estar autenticado
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}

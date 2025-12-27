package com.example.booking.security;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                        // Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public hotel browsing endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/hotels/**").permitAll()

                        // Check availability or Advanced Search (POST)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/hotels/search/**").permitAll()

                        // Public review reading endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/**").permitAll()

                        // Public image viewing endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/images/**").permitAll()

                        // Development tools
                        .requestMatchers("/h2-console/**").permitAll()

                        // API Documentation
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Promotions Endpoints (Admin Only) - Handled in Controller for Debugging
                        // .requestMatchers("/api/promotions/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                // H2 Console uses frames
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

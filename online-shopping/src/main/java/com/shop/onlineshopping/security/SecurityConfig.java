package com.shop.onlineshopping.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // When a request is denied, Spring asks one of two handlers to respond:
                //  - authenticationEntryPoint: the caller is NOT authenticated (no/invalid token) → 401
                //  - (default) accessDeniedHandler: the caller IS authenticated but lacks the role → 403
                // Without the entry point below, Spring returns 403 for both, so the frontend
                // cannot tell "please log in" (401) from "you're not allowed" (403).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authEx) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/stripe/webhook", "/ws/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                // Spring's internal error dispatch. If this is NOT permitted, any
                                // unhandled server error is re-dispatched to /error, blocked here,
                                // and surfaces to the client as a misleading empty 403 instead of
                                // the real 500. Permitting it lets real error responses through.
                                "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.ccp.portal.config;

import com.ccp.portal.security.RoleBasedAuthenticationSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Authentication: form login backed by the users table (PortalUserDetailsService),
 * passwords hashed with BCrypt.
 *
 * Authorization (URL level):
 *   public          -> home, login, register, static files, health check
 *   COMPLAINANT     -> submit complaints, "My complaints"
 *   REVIEWER, ADMIN -> /reviewer/**
 *   ADMIN           -> /admin/**
 *   any logged-in   -> tracking, documents, account (ownership checked in services)
 *
 * CSRF protection stays enabled; Thymeleaf th:action adds the token to every form.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RoleBasedAuthenticationSuccessHandler successHandler;

    public SecurityConfig(RoleBasedAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/login", "/register", "/access-denied", "/error",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/.well-known/**"
                ).permitAll()
                .requestMatchers(
                        "/actuator/health", "/actuator/health/**", "/actuator/info", "/api/hello"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reviewer/**").hasAnyRole("REVIEWER", "ADMIN")
                .requestMatchers("/complaint/new", "/complaint/my", "/api/complaints").hasRole("COMPLAINANT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}

package com.ccp.portal.config;

import com.ccp.portal.model.User;
import com.ccp.portal.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the initial ADMIN and REVIEWER accounts on start-up.
 *
 * Credentials come from environment variables (APP_ADMIN_EMAIL,
 * APP_ADMIN_PASSWORD, APP_REVIEWER_EMAIL, APP_REVIEWER_PASSWORD) with
 * development defaults in application.properties.
 *
 * Existing accounts are never overwritten, except the legacy MVP reviewer
 * whose password was the placeholder "TEMPORARY": that one is upgraded to
 * a real BCrypt password so it can log in.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.name}")
    private String adminName;

    @Value("${app.security.admin.email}")
    private String adminEmail;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    @Value("${app.security.reviewer.name}")
    private String reviewerName;

    @Value("${app.security.reviewer.email}")
    private String reviewerEmail;

    @Value("${app.security.reviewer.password}")
    private String reviewerPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureAccount(adminName, adminEmail, adminPassword, User.Role.ADMIN);
        ensureAccount(reviewerName, reviewerEmail, reviewerPassword, User.Role.REVIEWER);
    }

    private void ensureAccount(String name, String email, String rawPassword, User.Role role) {

        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            log.warn("Skipping {} account seeding: email or password not configured", role);
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(
                existing -> {
                    if (!existing.hasUsablePassword()) {
                        existing.setPassword(passwordEncoder.encode(rawPassword));
                        existing.setRole(role);
                        userRepository.save(existing);
                        log.info("Upgraded legacy {} account {} with a hashed password", role, normalizedEmail);
                    }
                },
                () -> {
                    userRepository.save(User.builder()
                            .name(name)
                            .email(normalizedEmail)
                            .password(passwordEncoder.encode(rawPassword))
                            .role(role)
                            .build());
                    log.info("Created initial {} account {}", role, normalizedEmail);
                }
        );
    }
}

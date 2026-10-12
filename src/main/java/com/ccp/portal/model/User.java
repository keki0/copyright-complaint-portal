package com.ccp.portal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt hash of the password. Accounts created by the old MVP
     * contain the placeholder "TEMPORARY" and cannot log in until the
     * owner claims the account through the registration page.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Disabled accounts cannot log in. Stored as "disabled" (default
     * false) so that existing rows stay active when Hibernate adds the
     * column with ddl-auto=update.
     */
    @Column(nullable = false)
    private boolean disabled;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }

    public boolean isStaff() {
        return role == Role.REVIEWER || role == Role.ADMIN;
    }

    /** True when the password is a real BCrypt hash, not a legacy placeholder. */
    public boolean hasUsablePassword() {
        return password != null && password.startsWith("$2");
    }

    public enum Role {
        COMPLAINANT,
        REVIEWER,
        ADMIN
    }
}

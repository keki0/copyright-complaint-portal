package com.ccp.portal.security;

import com.ccp.portal.model.User;
import com.ccp.portal.repository.UserRepository;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves the logged-in user's database record from the Spring Security
 * context. The entity is re-read on every request so role changes and
 * account disabling made by an admin are reflected immediately.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    /** For pages that are already protected by the security filter chain. */
    public User requireCurrentUser() {
        return findCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}

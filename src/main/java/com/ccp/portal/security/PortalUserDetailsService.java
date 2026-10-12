package com.ccp.portal.security;

import com.ccp.portal.model.User;
import com.ccp.portal.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads portal users from MySQL for Spring Security.
 * The e-mail address is the username; the role becomes ROLE_<ROLE>.
 */
@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PortalUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        String normalizedEmail = email == null ? "" : email.trim();

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(user.isDisabled())
                .build();
    }
}

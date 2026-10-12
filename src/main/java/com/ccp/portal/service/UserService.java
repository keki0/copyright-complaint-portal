package com.ccp.portal.service;

import com.ccp.portal.dto.CreateUserRequest;
import com.ccp.portal.dto.PasswordChangeRequest;
import com.ccp.portal.dto.RegistrationRequest;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Self-registration always creates a COMPLAINANT.
     *
     * Complainants created by the old MVP (password "TEMPORARY") can claim
     * their account by registering with the same e-mail; their earlier
     * complaints stay linked because the same user row is reused.
     */
    @Transactional
    public User registerComplainant(RegistrationRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String email = normalizeEmail(request.getEmail());

        User existing = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (existing != null) {

            boolean claimableLegacyAccount =
                    !existing.hasUsablePassword()
                            && existing.getRole() == User.Role.COMPLAINANT;

            if (!claimableLegacyAccount) {
                throw new IllegalArgumentException(
                        "An account with this email already exists. Please log in.");
            }

            existing.setName(request.getName().trim());
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
            return userRepository.save(existing);
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.COMPLAINANT)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, PasswordChangeRequest request) {

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ---------------------------------------------------------------- admin

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByRoleAscNameAsc();
    }

    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional
    public User createUser(CreateUserRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User changeRole(Long userId, User.Role newRole, User actingAdmin) {

        User user = findUser(userId);
        preventSelfModification(user, actingAdmin);

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleDisabled(Long userId, User actingAdmin) {

        User user = findUser(userId);
        preventSelfModification(user, actingAdmin);

        user.setDisabled(!user.isDisabled());
        return userRepository.save(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    /** An admin must not lock themselves out or remove their own admin rights. */
    private void preventSelfModification(User target, User actingAdmin) {
        if (target.getId().equals(actingAdmin.getId())) {
            throw new AccessDeniedException("You cannot change your own role or status.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

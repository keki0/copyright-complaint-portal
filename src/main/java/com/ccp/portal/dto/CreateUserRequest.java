package com.ccp.portal.dto;

import com.ccp.portal.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Used by an ADMIN to create reviewer / admin accounts. */
@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must contain 8 to 64 characters")
    @Pattern(
            regexp = PasswordRules.PATTERN,
            message = PasswordRules.MESSAGE
    )
    private String password;

    @NotNull(message = "Role is required")
    private User.Role role = User.Role.REVIEWER;
}

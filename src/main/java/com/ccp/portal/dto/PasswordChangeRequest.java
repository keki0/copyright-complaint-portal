package com.ccp.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "Password must contain 8 to 64 characters")
    @Pattern(
            regexp = PasswordRules.PATTERN,
            message = PasswordRules.MESSAGE
    )
    private String newPassword;

    @NotBlank(message = "Please confirm the new password")
    private String confirmPassword;
}

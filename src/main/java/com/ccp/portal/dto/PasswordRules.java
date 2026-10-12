package com.ccp.portal.dto;

/** Shared password policy used by every form that sets a password. */
public final class PasswordRules {

    /** At least one letter and at least one digit. Length is checked by @Size. */
    public static final String PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).+$";

    public static final String MESSAGE =
            "Password must contain at least one letter and one number";

    private PasswordRules() {
    }
}

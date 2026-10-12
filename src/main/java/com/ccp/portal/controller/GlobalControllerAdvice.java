package com.ccp.portal.controller;

import com.ccp.portal.exception.ResourceNotFoundException;
import com.ccp.portal.model.User;
import com.ccp.portal.security.CurrentUserService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 1. Exposes the logged-in user to every Thymeleaf page as ${currentUser}
 *    (used by the navigation bar to show role-specific links).
 * 2. Converts "not found" and "access denied" errors into friendly pages.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final CurrentUserService currentUserService;

    public GlobalControllerAdvice(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return currentUserService.findCurrentUser().orElse(null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(
            ResourceNotFoundException ex,
            HttpServletResponse response,
            Model model) {

        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("currentUser", currentUser());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException ex,
            HttpServletResponse response,
            Model model) {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        model.addAttribute("currentUser", currentUser());
        model.addAttribute("message", ex.getMessage());
        return "access-denied";
    }
}

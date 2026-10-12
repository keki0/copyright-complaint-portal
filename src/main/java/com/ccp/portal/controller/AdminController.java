package com.ccp.portal.controller;

import com.ccp.portal.dto.CreateUserRequest;
import com.ccp.portal.model.User;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.ReviewService;
import com.ccp.portal.service.UserService;

import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** User management for administrators. */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    public AdminController(
            UserService userService,
            ReviewService reviewService,
            CurrentUserService currentUserService) {

        this.userService = userService;
        this.reviewService = reviewService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/users")
    public String users(Model model) {

        if (!model.containsAttribute("createUserRequest")) {
            model.addAttribute("createUserRequest", new CreateUserRequest());
        }

        populate(model);
        return "admin-users";
    }

    @PostMapping("/users")
    public String createUser(
            @Valid @ModelAttribute("createUserRequest") CreateUserRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            populate(model);
            return "admin-users";
        }

        try {
            User user = userService.createUser(request);
            redirectAttributes.addFlashAttribute("success",
                    "Created " + user.getRole() + " account for " + user.getEmail() + ".");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            populate(model);
            return "admin-users";
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam User.Role role,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.changeRole(id, role, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success",
                    user.getEmail() + " is now " + user.getRole() + ".");
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleDisabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            User user = userService.toggleDisabled(id, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success",
                    user.getEmail() + (user.isDisabled() ? " has been disabled." : " has been enabled."));
        } catch (IllegalArgumentException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    private void populate(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("complainantCount", userService.countByRole(User.Role.COMPLAINANT));
        model.addAttribute("reviewerCount", userService.countByRole(User.Role.REVIEWER));
        model.addAttribute("adminCount", userService.countByRole(User.Role.ADMIN));
        model.addAttribute("counts", reviewService.getStatusCounts());
    }
}

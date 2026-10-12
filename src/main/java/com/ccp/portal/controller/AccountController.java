package com.ccp.portal.controller;

import com.ccp.portal.dto.PasswordChangeRequest;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** "My account" page available to every logged-in user. */
@Controller
public class AccountController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AccountController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/account")
    public String account(Model model) {

        if (!model.containsAttribute("passwordChangeRequest")) {
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
        }

        return "account";
    }

    @PostMapping("/account/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "account";
        }

        try {
            userService.changePassword(currentUserService.requireCurrentUser(), request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "account";
        }

        redirectAttributes.addFlashAttribute("success", "Your password has been changed.");
        return "redirect:/account";
    }
}

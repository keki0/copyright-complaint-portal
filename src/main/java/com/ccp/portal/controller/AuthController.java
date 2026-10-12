package com.ccp.portal.controller;

import com.ccp.portal.dto.RegistrationRequest;
import com.ccp.portal.model.User;
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

@Controller
public class AuthController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AuthController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /** Custom login page. The POST /login itself is handled by Spring Security. */
    @GetMapping("/login")
    public String loginPage() {

        User currentUser = currentUserService.findCurrentUser().orElse(null);

        if (currentUser != null) {
            return "redirect:" + homePageFor(currentUser);
        }

        return "login";
    }

    @GetMapping("/register")
    public String registrationPage(Model model) {

        User currentUser = currentUserService.findCurrentUser().orElse(null);

        if (currentUser != null) {
            return "redirect:" + homePageFor(currentUser);
        }

        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerComplainant(request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute(
                "success", "Account created. Please log in with your email and password.");

        return "redirect:/login?registered";
    }

    static String homePageFor(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "/admin/users";
            case REVIEWER -> "/reviewer/dashboard";
            case COMPLAINANT -> "/complaint/my";
        };
    }
}

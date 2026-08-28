package com.ccp.portal.controller;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.service.ReviewService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviewer")
public class ReviewerController {

    private final ReviewService reviewService;

    public ReviewerController(
            ReviewService reviewService) {

        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "complaints",
                reviewService.getPendingComplaints()
        );

        return "reviewer-dashboard";
    }

    @GetMapping("/complaint/{id}")
    public String viewComplaint(
            @PathVariable Long id,
            Model model) {

        Complaint complaint =
                reviewService.getComplaint(id);

        model.addAttribute(
                "complaint",
                complaint
        );

        return "reviewer-detail";
    }

    @PostMapping("/complaint/{id}/review")
    public String reviewComplaint(
            @PathVariable Long id,

            @RequestParam String decision,

            @RequestParam String remarks) {

        reviewService.reviewComplaint(
                id,
                decision,
                remarks
        );

        return "redirect:/reviewer/dashboard";
    }
}
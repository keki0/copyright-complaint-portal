package com.ccp.portal.controller;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.User;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.ComplaintHistoryService;
import com.ccp.portal.service.ComplaintService;
import com.ccp.portal.service.ReviewService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Reviewer workspace. Protected twice: by the URL rule /reviewer/** in
 * SecurityConfig and by method security (@PreAuthorize) here.
 */
@Controller
@RequestMapping("/reviewer")
@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
public class ReviewerController {

    private final ReviewService reviewService;
    private final ComplaintService complaintService;
    private final ComplaintHistoryService historyService;
    private final CurrentUserService currentUserService;

    public ReviewerController(
            ReviewService reviewService,
            ComplaintService complaintService,
            ComplaintHistoryService historyService,
            CurrentUserService currentUserService) {

        this.reviewService = reviewService;
        this.complaintService = complaintService;
        this.historyService = historyService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "q", required = false) String query,
            Model model) {

        User reviewer = currentUserService.requireCurrentUser();
        String activeFilter = reviewService.normalizeFilter(filter);

        model.addAttribute("complaints",
                reviewService.getDashboardComplaints(activeFilter, query, reviewer));
        Map<Complaint.Status, Long> counts = reviewService.getStatusCounts();

        model.addAttribute("counts", counts);
        model.addAttribute("pendingCount",
                counts.get(Complaint.Status.SUBMITTED) + counts.get(Complaint.Status.UNDER_REVIEW));
        model.addAttribute("filters", ReviewService.FILTERS);
        model.addAttribute("activeFilter", activeFilter);
        model.addAttribute("query", query == null ? "" : query);

        return "reviewer-dashboard";
    }

    @GetMapping("/complaint/{id}")
    public String viewComplaint(@PathVariable Long id, Model model) {

        User reviewer = currentUserService.requireCurrentUser();
        Complaint complaint = reviewService.getComplaint(id);

        model.addAttribute("complaint", complaint);
        model.addAttribute("documents", complaintService.getDocuments(complaint));
        model.addAttribute("history", historyService.getHistory(complaint));
        model.addAttribute("canDecide", reviewService.canDecide(complaint, reviewer));
        reviewService.getLatestReview(complaint)
                .ifPresent(review -> model.addAttribute("review", review));

        return "reviewer-detail";
    }

    @PostMapping("/complaint/{id}/start")
    public String startReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            reviewService.startReview(id, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Review started. The complaint is now assigned to you.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reviewer/complaint/" + id;
    }

    @PostMapping("/complaint/{id}/review")
    public String reviewComplaint(
            @PathVariable Long id,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) String remarks,
            RedirectAttributes redirectAttributes) {

        try {
            Complaint complaint = reviewService.reviewComplaint(
                    id, decision, remarks, currentUserService.requireCurrentUser());

            redirectAttributes.addFlashAttribute("success",
                    "Complaint " + complaint.getComplaintNumber() + " was " + complaint.getStatus() + ".");

            return "redirect:/reviewer/dashboard";

        } catch (IllegalArgumentException | IllegalStateException | AccessDeniedException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviewer/complaint/" + id;
        }
    }
}

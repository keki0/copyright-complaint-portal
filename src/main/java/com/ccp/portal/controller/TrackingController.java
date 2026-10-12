package com.ccp.portal.controller;

import com.ccp.portal.exception.ResourceNotFoundException;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.User;
import com.ccp.portal.repository.ReviewRepository;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.ComplaintHistoryService;
import com.ccp.portal.service.ComplaintService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/complaint")
public class TrackingController {

    private final ComplaintService complaintService;
    private final ReviewRepository reviewRepository;
    private final ComplaintHistoryService historyService;
    private final CurrentUserService currentUserService;

    public TrackingController(
            ComplaintService complaintService,
            ReviewRepository reviewRepository,
            ComplaintHistoryService historyService,
            CurrentUserService currentUserService) {

        this.complaintService = complaintService;
        this.reviewRepository = reviewRepository;
        this.historyService = historyService;
        this.currentUserService = currentUserService;
    }

    /** GET /complaint/track            -> empty search form
     *  GET /complaint/track?id=CCP-...  -> result (used by "My complaints" links) */
    @GetMapping("/track")
    public String trackingPage(
            @RequestParam(value = "id", required = false) String complaintNumber,
            Model model) {

        if (complaintNumber == null || complaintNumber.isBlank()) {
            return "track";
        }

        return showComplaint(complaintNumber, model);
    }

    @PostMapping("/track")
    public String trackComplaint(@RequestParam String complaintNumber, Model model) {
        return showComplaint(complaintNumber, model);
    }

    private String showComplaint(String complaintNumber, Model model) {

        User viewer = currentUserService.requireCurrentUser();
        model.addAttribute("searchedNumber", complaintNumber.trim().toUpperCase());

        Complaint complaint;

        try {
            complaint = complaintService.getComplaintForViewer(complaintNumber, viewer);
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "track";
        }

        model.addAttribute("complaint", complaint);
        model.addAttribute("documents", complaintService.getDocuments(complaint));
        model.addAttribute("history", historyService.getHistory(complaint));

        reviewRepository
                .findTopByComplaintOrderByReviewedAtDesc(complaint)
                .ifPresent(review -> model.addAttribute("review", review));

        return "track";
    }
}

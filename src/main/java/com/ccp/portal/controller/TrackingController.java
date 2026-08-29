package com.ccp.portal.controller;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.Review;
import com.ccp.portal.repository.ComplaintRepository;
import com.ccp.portal.repository.ReviewRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/complaint")
public class TrackingController {

    private final ComplaintRepository complaintRepository;
    private final ReviewRepository reviewRepository;

    public TrackingController(
            ComplaintRepository complaintRepository,
            ReviewRepository reviewRepository) {

        this.complaintRepository = complaintRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/track")
    public String trackingPage() {

        return "track";
    }

    @PostMapping("/track")
    public String trackComplaint(
            @RequestParam String complaintNumber,
            Model model) {

        String normalizedNumber =
                complaintNumber.trim().toUpperCase();

        Complaint complaint =
                complaintRepository
                        .findByComplaintNumber(normalizedNumber)
                        .orElse(null);

        if (complaint == null) {

            model.addAttribute(
                    "error",
                    "No complaint was found with ID: "
                            + normalizedNumber
            );

            return "track";
        }

        model.addAttribute(
                "complaint",
                complaint
        );

        Review review =
                reviewRepository
                        .findTopByComplaintOrderByReviewedAtDesc(
                                complaint
                        )
                        .orElse(null);

        if (review != null) {

            model.addAttribute(
                    "review",
                    review
            );
        }

        return "track";
    }
}
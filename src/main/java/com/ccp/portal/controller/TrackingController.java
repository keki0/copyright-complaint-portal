package com.ccp.portal.controller;

import com.ccp.portal.model.Complaint;
import com.ccp.portal.repository.ComplaintRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/complaint")
public class TrackingController {

    private final ComplaintRepository complaintRepository;

    public TrackingController(
            ComplaintRepository complaintRepository) {

        this.complaintRepository =
                complaintRepository;
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
                        .findByComplaintNumber(
                                normalizedNumber
                        )
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

        return "track";
    }
}
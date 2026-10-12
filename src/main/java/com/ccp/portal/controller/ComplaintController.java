package com.ccp.portal.controller;

import com.ccp.portal.dto.ComplaintRequest;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.model.User;
import com.ccp.portal.security.CurrentUserService;
import com.ccp.portal.service.ComplaintService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class ComplaintController {

    private static final Logger log = LoggerFactory.getLogger(ComplaintController.class);

    private final ComplaintService complaintService;
    private final CurrentUserService currentUserService;

    public ComplaintController(
            ComplaintService complaintService,
            CurrentUserService currentUserService) {

        this.complaintService = complaintService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/api/complaints")
    public String submitComplaint(
            @Valid @ModelAttribute("complaintRequest") ComplaintRequest request,
            BindingResult result,
            @RequestParam(value = "document", required = false) MultipartFile document,
            Model model) {

        model.addAttribute("copyrightTypes", PageController.COPYRIGHT_TYPES);

        if (result.hasErrors()) {
            model.addAttribute("error", "Please correct the highlighted fields.");
            return "complaint-form";
        }

        User complainant = currentUserService.requireCurrentUser();

        try {

            Complaint complaint =
                    complaintService.submitComplaint(request, document, complainant);

            model.addAttribute("complaint", complaint);
            return "complaint-success";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            return "complaint-form";

        } catch (Exception e) {

            log.error("Complaint submission failed", e);
            model.addAttribute("error", "Unable to process the document. Please try again.");
            return "complaint-form";
        }
    }

    /** Complainant dashboard: every complaint filed by the logged-in user. */
    @GetMapping("/complaint/my")
    public String myComplaints(Model model) {

        User complainant = currentUserService.requireCurrentUser();
        List<Complaint> complaints = complaintService.getComplaintsOf(complainant);

        model.addAttribute("complaints", complaints);
        model.addAttribute("totalCount", complaints.size());
        model.addAttribute("openCount", complaints.stream().filter(c -> !c.isFinalized()).count());
        model.addAttribute("approvedCount", complaints.stream()
                .filter(c -> c.getStatus() == Complaint.Status.APPROVED).count());
        model.addAttribute("rejectedCount", complaints.stream()
                .filter(c -> c.getStatus() == Complaint.Status.REJECTED).count());

        return "my-complaints";
    }
}

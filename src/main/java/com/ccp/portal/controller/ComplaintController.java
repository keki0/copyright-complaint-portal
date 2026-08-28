package com.ccp.portal.controller;

import com.ccp.portal.dto.ComplaintRequest;
import com.ccp.portal.model.Complaint;
import com.ccp.portal.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@Controller
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(
            ComplaintService complaintService) {

        this.complaintService = complaintService;
    }

    @PostMapping("/api/complaints")
    public String submitComplaint(

            @Valid @ModelAttribute ComplaintRequest request,

            BindingResult result,

            @RequestParam("document")
            MultipartFile document,

            Model model) {

        if (result.hasErrors()) {
            return "complaint-form";
        }

        try {

            Complaint complaint =
                    complaintService.submitComplaint(
                            request,
                            document
                    );

            model.addAttribute(
                    "complaint",
                    complaint
            );

            return "complaint-success";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "complaint-form";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Unable to process the document. Please try again."
            );

            return "complaint-form";
        }
    }
}
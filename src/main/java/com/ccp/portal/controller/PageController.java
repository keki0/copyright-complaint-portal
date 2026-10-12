package com.ccp.portal.controller;

import com.ccp.portal.dto.ComplaintRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    public static final List<String> COPYRIGHT_TYPES = List.of(
            "Literary Work", "Music", "Film/Video", "Software", "Photography", "Artwork", "Other");

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/complaint/new")
    public String complaintForm(Model model) {

        if (!model.containsAttribute("complaintRequest")) {
            model.addAttribute("complaintRequest", new ComplaintRequest());
        }

        model.addAttribute("copyrightTypes", COPYRIGHT_TYPES);
        return "complaint-form";
    }
}

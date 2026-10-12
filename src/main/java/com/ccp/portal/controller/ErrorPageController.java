package com.ccp.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {

    /**
     * Target of Spring Security's accessDeniedPage(). Mapped for every HTTP
     * method because a denied POST is forwarded here as a POST.
     */
    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}

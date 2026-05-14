package com.nazarukiv.scrapepilotai.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String disabled,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            Authentication authentication,
            Model model
    ) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("loginError", error != null);
        model.addAttribute("accountDisabled", disabled != null);
        model.addAttribute("loggedOut", logout != null);
        model.addAttribute("sessionExpired", expired != null);
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}

package com.nazarukiv.scrapepilotai.controller;

import com.nazarukiv.scrapepilotai.dto.AuthenticatedUserDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AuthenticationModelAdvice {

    private static final String ROLE_PREFIX = "ROLE_";

    @ModelAttribute("authenticatedUser")
    public AuthenticatedUserDto authenticatedUser(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        return new AuthenticatedUserDto(
                authentication.getName(),
                roleLabel(authentication)
        );
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String roleLabel(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .findFirst()
                .orElse("AUTHENTICATED");
    }
}

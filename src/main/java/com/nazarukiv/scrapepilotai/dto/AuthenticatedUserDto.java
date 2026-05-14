package com.nazarukiv.scrapepilotai.dto;

public record AuthenticatedUserDto(
        String username,
        String roleLabel
) {
}

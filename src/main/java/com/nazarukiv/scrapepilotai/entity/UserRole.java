package com.nazarukiv.scrapepilotai.entity;

public enum UserRole {
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}

package com.nazarukiv.scrapepilotai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scrapepilot.security.default-admin")
public class DefaultAdminProperties {

    private boolean createIfMissing = true;
    private String username = "admin";
    private String password = "admin123";
    private boolean enabled = true;

    public boolean isCreateIfMissing() {
        return createIfMissing;
    }

    public void setCreateIfMissing(boolean createIfMissing) {
        this.createIfMissing = createIfMissing;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

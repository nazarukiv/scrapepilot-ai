package com.nazarukiv.scrapepilotai.config;

import com.nazarukiv.scrapepilotai.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private final UserService userService;
    private final DefaultAdminProperties defaultAdminProperties;

    public DefaultAdminInitializer(
            UserService userService,
            DefaultAdminProperties defaultAdminProperties
    ) {
        this.userService = userService;
        this.defaultAdminProperties = defaultAdminProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!defaultAdminProperties.isCreateIfMissing()) {
            LOGGER.info("Default admin bootstrap is disabled");
            return;
        }

        boolean created = userService.createDefaultAdminIfMissing(
                defaultAdminProperties.getUsername(),
                defaultAdminProperties.getPassword(),
                defaultAdminProperties.isEnabled()
        );

        if (created) {
            LOGGER.warn(
                    "Created default admin user '{}'. Set SCRAPEPILOT_ADMIN_USERNAME and SCRAPEPILOT_ADMIN_PASSWORD before production use.",
                    defaultAdminProperties.getUsername()
            );
        }
    }
}

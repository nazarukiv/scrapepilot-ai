package com.nazarukiv.scrapepilotai.service;

import com.nazarukiv.scrapepilotai.entity.User;
import com.nazarukiv.scrapepilotai.entity.UserRole;
import com.nazarukiv.scrapepilotai.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User loadByUsername(String username) {
        return userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));
    }

    @Transactional
    public boolean createDefaultAdminIfMissing(String username, String rawPassword, boolean enabled) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return false;
        }

        String normalizedUsername = normalizeUsername(username);
        validateBootstrapCredential("username", normalizedUsername);
        validateBootstrapCredential("password", rawPassword);

        User admin = new User(
                normalizedUsername,
                passwordEncoder.encode(rawPassword),
                UserRole.ADMIN,
                enabled
        );
        userRepository.save(admin);
        return true;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void validateBootstrapCredential(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Default admin " + field + " must not be blank");
        }
    }
}

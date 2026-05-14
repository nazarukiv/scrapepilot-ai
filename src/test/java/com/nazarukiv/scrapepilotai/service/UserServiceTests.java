package com.nazarukiv.scrapepilotai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nazarukiv.scrapepilotai.entity.User;
import com.nazarukiv.scrapepilotai.entity.UserRole;
import com.nazarukiv.scrapepilotai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createsDefaultAdminWithBcryptPasswordHash() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        boolean created = userService.createDefaultAdminIfMissing(" Admin ", "admin123", true);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertTrue(created);
        assertEquals("admin", savedUser.getUsername());
        assertEquals(UserRole.ADMIN, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
        assertFalse("admin123".equals(savedUser.getPasswordHash()));
        assertTrue(passwordEncoder.matches("admin123", savedUser.getPasswordHash()));
    }

    @Test
    void skipsDefaultAdminCreationWhenAdminAlreadyExists() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        boolean created = userService.createDefaultAdminIfMissing("admin", "admin123", true);

        assertFalse(created);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsBlankDefaultAdminPassword() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.createDefaultAdminIfMissing("admin", " ", true)
        );

        assertEquals("Default admin password must not be blank", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}

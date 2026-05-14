package com.nazarukiv.scrapepilotai.repository;

import com.nazarukiv.scrapepilotai.entity.User;
import com.nazarukiv.scrapepilotai.entity.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByRole(UserRole role);
}

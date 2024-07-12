package com.switix.userservice.repository;

import com.switix.userservice.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);
}

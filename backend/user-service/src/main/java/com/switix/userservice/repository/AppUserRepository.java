package com.switix.userservice.repository;

import com.switix.userservice.model.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppUserRepository extends MongoRepository<AppUser, String> {

    Boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);
}

package com.smarttravel.modules.user.repository;

import com.smarttravel.modules.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNormalizedEmail(String normalizedEmail);

    boolean existsByEmail(String email);

    boolean existsByNormalizedEmail(String normalizedEmail);
}

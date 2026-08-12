package com.messmate.backend.repository;

import com.messmate.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubjectId(String googleSubjectId);

    Boolean existsByEmail(String email);

    Boolean existsByGoogleSubjectId(String googleSubjectId);
}

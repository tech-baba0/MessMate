package com.messmate.backend.repository;

import com.messmate.backend.entity.Mess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessRepository extends MongoRepository<Mess, String> {
    Optional<Mess> findByInviteCode(String inviteCode);
}

package com.messmate.backend.repository;

import com.messmate.backend.entity.MessMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessMemberRepository extends MongoRepository<MessMember, String> {
    List<MessMember> findByMessId(String messId);
    List<MessMember> findByUserId(String userId);
    Optional<MessMember> findByMessIdAndUserId(String messId, String userId);
}

package com.messmate.backend.repository;

import com.messmate.backend.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByMessId(String messId);

    List<Payment> findByMessIdAndDateBetween(String messId, LocalDate startDate, LocalDate endDate);
}

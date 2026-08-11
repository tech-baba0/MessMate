package com.messmate.backend.repository;

import com.messmate.backend.entity.MonthlySettlement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySettlementRepository extends MongoRepository<MonthlySettlement, String> {
    Optional<MonthlySettlement> findByMessIdAndMonthYear(String messId, String monthYear);

    List<MonthlySettlement> findByMessId(String messId);
}

package com.messmate.backend.repository;

import com.messmate.backend.entity.ExpenseShare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseShareRepository extends MongoRepository<ExpenseShare, String> {
    List<ExpenseShare> findByExpenseId(String expenseId);
    List<ExpenseShare> findByMessIdAndUserId(String messId, String userId);
}

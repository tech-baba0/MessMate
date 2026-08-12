package com.messmate.backend.repository;

import com.messmate.backend.entity.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends MongoRepository<Menu, String> {
    List<Menu> findByMessId(String messId);

    List<Menu> findByMessIdAndIsPublishedTrue(String messId);

    Optional<Menu> findByMessIdAndDayOfWeek(String messId, Integer dayOfWeek);

    Optional<Menu> findByMessIdAndDayOfWeekAndIsPublishedTrue(String messId, Integer dayOfWeek);
}

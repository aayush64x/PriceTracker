package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    List<PriceHistory> findByProduct_ASIN(String ASIN);
}

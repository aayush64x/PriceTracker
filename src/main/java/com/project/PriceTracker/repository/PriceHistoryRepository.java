package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    Optional<PriceHistory> getByProductId(int productId);
}

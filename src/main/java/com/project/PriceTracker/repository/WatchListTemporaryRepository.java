package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.WatchListTemporary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchListTemporaryRepository extends JpaRepository<WatchListTemporary, Integer> {
    Optional<WatchListTemporary> findByUserTemporaryEmail(String email);
}

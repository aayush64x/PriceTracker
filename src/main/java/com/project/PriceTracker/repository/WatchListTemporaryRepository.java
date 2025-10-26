package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.WatchListTemporary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface WatchListTemporaryRepository extends JpaRepository<WatchListTemporary, Integer> {
    List<WatchListTemporary> findByUserTemporaryEmail(String email);
}

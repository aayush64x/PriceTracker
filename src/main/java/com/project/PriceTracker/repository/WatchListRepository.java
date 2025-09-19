package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchListRepository extends JpaRepository<WatchList, Integer> {
    List<WatchList> findByUserId(int userId);
}

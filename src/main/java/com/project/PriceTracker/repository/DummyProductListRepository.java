package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.DummyProductList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyProductListRepository extends JpaRepository<DummyProductList, Integer> {
}

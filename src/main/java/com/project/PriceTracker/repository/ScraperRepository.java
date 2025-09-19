package com.project.PriceTracker.repository;

import com.project.PriceTracker.model.DummyProductList;
import com.project.PriceTracker.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScraperRepository extends JpaRepository<DummyProductList, Integer> {
}

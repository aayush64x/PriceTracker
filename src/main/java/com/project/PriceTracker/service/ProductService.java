package com.project.PriceTracker.service;

import com.project.PriceTracker.model.DummyProductList;
import com.project.PriceTracker.repository.ScraperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ScraperRepository repository;

    public ProductService(ScraperRepository repository){
        this.repository = repository;
    }
    public List<DummyProductList> getAllProducts(){
        return repository.findAll();
    }
}

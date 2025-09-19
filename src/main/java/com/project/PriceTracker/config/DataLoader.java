package com.project.PriceTracker.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.PriceTracker.model.DummyProductList;
import com.project.PriceTracker.repository.DummyProductListRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;


public class DataLoader {
    @Autowired
    private DummyProductListRepository repository;

    @PostConstruct
    public void loadJsonData(){
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream inputStream = new ClassPathResource("example.json").getInputStream();

            List<DummyProductList> products = mapper.readValue(inputStream, new TypeReference<List<DummyProductList>>() {});

            repository.saveAll(products);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


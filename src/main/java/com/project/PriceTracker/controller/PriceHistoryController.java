package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.PriceHistoryDTO;
import com.project.PriceTracker.service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PriceHistoryController {

    @Autowired
    public PriceHistoryService priceHistoryService;


    @GetMapping("/price-history")
    public List<PriceHistoryDTO> getPriceHistory(@RequestParam String q){
        return priceHistoryService.getPriceHistory(q);
    }
}

package com.project.PriceTracker.controller;

import com.project.PriceTracker.dto.WatchListRequestDTO;
import com.project.PriceTracker.service.UserTemporaryService;
import com.project.PriceTracker.service.WatchListTemporaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist/")
public class WatchListController {

    private WatchListTemporaryService watchListTemporaryService;
    private UserTemporaryService userTemporaryService;

    @Autowired
    public WatchListController(WatchListTemporaryService watchListTemporaryService, UserTemporaryService userTemporaryService){
        this.watchListTemporaryService = watchListTemporaryService;
        this.userTemporaryService = userTemporaryService;
    }



    @PostMapping("/add")
    public ResponseEntity<String> addToWatchList(@RequestBody WatchListRequestDTO request){

        watchListTemporaryService.addToWatchList(request);
        return ResponseEntity.ok("Added to watchList");
    }



}


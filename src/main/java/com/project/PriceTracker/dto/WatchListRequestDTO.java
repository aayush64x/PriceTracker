package com.project.PriceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchListRequestDTO {
    private String email;
    private Double targetPrice; // Watchlist target price
    private String asin;
}
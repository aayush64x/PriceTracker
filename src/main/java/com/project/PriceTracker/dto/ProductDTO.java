package com.project.PriceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private int id;
    private String ASIN;
    private String productName;
    private Double price;
    private String link;
    private String imageURL;
    private Timestamp timeStamp;
    private String productGroup;
    private String category;
}

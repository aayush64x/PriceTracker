package com.project.PriceTracker.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int productId;
    private String ASIN;
    private String productName;
    private Double productPrice;
    private String link;
    private String imageURL;
    private String category;
    private Timestamp timestamp;
    private String productGroup;



}

package com.project.PriceTracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class DummyProductList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JsonProperty("ProductName")
    private String productName;
    @JsonProperty("Price")
    private String price;
    @JsonProperty("Link")
    private String link;

    public DummyProductList() {}

    public DummyProductList(String productName, String price, String link) {
        this.productName = productName;
        this.price = price;
        this.link = link;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}

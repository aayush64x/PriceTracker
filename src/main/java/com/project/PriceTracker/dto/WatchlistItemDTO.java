package com.project.PriceTracker.dto;

public class WatchlistItemDTO {
    // Watchlist specific fields
    private Integer id; // watchlist ID
    private Double targetPrice;

    // Product fields
    private String asin;
    private String productName;
    private Double price;
    private String imageURL;
    private String url;
    private String category;
    private String brand;

    // Constructors
    public WatchlistItemDTO() {
    }

    public WatchlistItemDTO(Integer id, Double targetPrice, String asin, String productName,
                            Double price, String imageURL, String url, String category, String brand) {
        this.id = id;
        this.targetPrice = targetPrice;
        this.asin = asin;
        this.productName = productName;
        this.price = price;
        this.imageURL = imageURL;
        this.url = url;
        this.category = category;
        this.brand = brand;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
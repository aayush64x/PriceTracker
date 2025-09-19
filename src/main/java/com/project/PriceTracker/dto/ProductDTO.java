package com.project.PriceTracker.dto;

public class ProductDTO {

    private int id;
    private String productName;
    private String price;
    private String Link;

    public ProductDTO(){};

    public ProductDTO(int id, String productName, String price, String link) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        Link = link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }
}

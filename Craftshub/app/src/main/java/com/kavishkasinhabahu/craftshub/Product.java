package com.kavishkasinhabahu.craftshub;

public class Product {

    private String id;
    private String name;
    private String image1;
    private String quantity;
    private String price;
    private String description;
    private String category;
    private String seller;
    private String documentId;
    private double sellQuantity;

    public Product() {}

    public Product(String name, String image1, String price, String quantity, String description, String category, String seller, String documentId) {
        this.name = name;
        this.image1 = image1;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.category = category;
        this.seller = seller;
        this.documentId = documentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDescription() {
        return description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getSeller() {
        return seller;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSellQuantity(double sellQuantity) {
        this.sellQuantity = sellQuantity;
    }

    public double getSellQuantity() {
        return sellQuantity;
    }
}
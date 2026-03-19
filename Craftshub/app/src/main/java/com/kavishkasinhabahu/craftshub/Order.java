package com.kavishkasinhabahu.craftshub;

import com.google.firebase.Timestamp;

public class Order {
    private String orderId;
    private double fullAmount;
    private Timestamp orderAddedDatetime;
    private String status;

    public Order() { }

    public Order(String orderId, double fullAmount, Timestamp orderAddedDatetime, String status) {
        this.orderId = orderId;
        this.fullAmount = fullAmount;
        this.orderAddedDatetime = orderAddedDatetime;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public double getFullAmount() { return fullAmount; }
    public Timestamp getOrderAddedDatetime() { return orderAddedDatetime; }
    public String getStatus() { return status; }
}


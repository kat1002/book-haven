package com.son.bookhaven.data.model;

import java.io.Serializable;

public class OrderDetail implements Serializable {
    private int orderDetailId;
    private int orderId;
    private int bookId;
    private int quantity;
    private double unitPrice;
    private double subTotal;
    private String bookName;
    private double pricePerUnit;

    public OrderDetail(int orderDetailId, int orderId, int bookId, String bookName, int quantity, double pricePerUnit) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subTotal = quantity * pricePerUnit;
    }

    public OrderDetail() {

    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }
}

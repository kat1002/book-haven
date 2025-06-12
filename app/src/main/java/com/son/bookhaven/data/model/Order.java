package com.son.bookhaven.data.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int oderId;
    private int userId;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private String district;
    private String city;
    private String ward;
    private String street;
    private String recipientName;
    private String note;
    private double discountedPrice;
    private byte paymentMethod;
    private String voucherCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int voucherId;
    private String cartKey;
    private List<OrderDetail> orderDetails;
    private User user;
}

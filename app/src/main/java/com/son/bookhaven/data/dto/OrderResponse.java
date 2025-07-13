package com.son.bookhaven.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class OrderResponse {
    @SerializedName("orderId")
    public int orderId;
    
    @SerializedName("userId")
    public Integer userId;
    
    @SerializedName("orderDate")
    public String orderDate; // We'll parse this as String first
    
    @SerializedName("totalAmount")
    public double totalAmount;
    
    @SerializedName("status")
    public String status;
    
    @SerializedName("district")
    public String district;
    
    @SerializedName("city")
    public String city;
    
    @SerializedName("ward")
    public String ward;
    
    @SerializedName("street")
    public String street;
    
    @SerializedName("recipientName")
    public String recipientName;
    
    @SerializedName("phoneNumber")
    public String phoneNumber;
    
    @SerializedName("note")
    public String note;
    
    @SerializedName("feedBack")
    public String feedBack;
    
    @SerializedName("paymentOrderCode")
    public Long paymentOrderCode;
    
    @SerializedName("discountedPrice")
    public double discountedPrice;
    
    @SerializedName("paymentMethod")
    public int paymentMethod;
    
    @SerializedName("voucherCode")
    public String voucherCode;
    
    @SerializedName("createdAt")
    public String createdAt;
    
    @SerializedName("updatedAt")
    public String updatedAt;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("voucherId")
    public Integer voucherId;
    
    @SerializedName("cartKey")
    public String cartKey;
    
    @SerializedName("orderDetails")
    public List<OrderDetailResponse> orderDetails = new ArrayList<>();

} 
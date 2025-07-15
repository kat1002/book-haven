package com.son.bookhaven.data.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
public class OrderDetailResponse {
    @SerializedName("orderId")
    public int orderId;
    
    @SerializedName("variantId")
    public int variantId;
    
    @SerializedName("quantity")
    public int quantity;
    
    @SerializedName("unitPrice")
    public double unitPrice;
    
    @SerializedName("subtotal")
    public double subtotal;
    
    @SerializedName("url")
    public String url;
    
    @SerializedName("title")
    public String title;
    
    @SerializedName("publicationYear")
    public int publicationYear;
    
    @SerializedName("language")
    public int language;
} 
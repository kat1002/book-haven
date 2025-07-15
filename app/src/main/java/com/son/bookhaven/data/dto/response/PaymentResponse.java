package com.son.bookhaven.data.dto.response;

public class PaymentResponse {
    private String paymentUrl;

    // This constructor allows direct string assignment
    public PaymentResponse(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public PaymentResponse() {
        // Default constructor required for Gson
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
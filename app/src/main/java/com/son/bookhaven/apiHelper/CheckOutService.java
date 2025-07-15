package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.request.CartItemUpdateRequest;
import com.son.bookhaven.data.dto.request.CheckOutRequest;
import com.son.bookhaven.data.dto.response.ApiResponse;
import com.son.bookhaven.data.dto.response.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CheckOutService {

    @POST("/api/Checkout/checkout")
    Call<ApiResponse<String>> placeOrder(@Body CheckOutRequest request);
}

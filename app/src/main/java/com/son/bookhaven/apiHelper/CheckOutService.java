package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.request.CartItemUpdateRequest;
import com.son.bookhaven.data.dto.request.CheckOutRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CheckOutService {

    @POST("/api/Checkout/checkout")
    Call<Void> placeOrder(@Body CheckOutRequest request);
}

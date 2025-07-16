package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.CheckOutRequest;
import com.son.bookhaven.data.dto.response.CheckOutResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CheckOutService {

    @POST("/api/Checkout/checkout")
    Call<ApiResponse<CheckOutResponse>> placeOrder(@Body CheckOutRequest request);
}

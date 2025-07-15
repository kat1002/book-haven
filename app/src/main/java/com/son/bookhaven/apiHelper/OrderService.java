package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.data.dto.PagedResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OrderService {
    @GET("api/order/user-orders")
    Call<ApiResponse<PagedResult<OrderResponse>>> getUserOrders(
            @Query("userId") int userId,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
} 
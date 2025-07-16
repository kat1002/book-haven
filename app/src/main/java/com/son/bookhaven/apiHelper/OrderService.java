package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.data.dto.PagedResult;
import com.son.bookhaven.data.dto.request.UpdateOrderRequest;
import com.son.bookhaven.data.dto.response.PaymentLinkInformation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderService {
    @GET("api/order/user-orders")
    Call<ApiResponse<PagedResult<OrderResponse>>> getUserOrders(
            @Query("userId") int userId,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/Order/payment-check")
    Call<ApiResponse<PaymentLinkInformation>> checkPaymentStatus(@Query("paymentCode") String paymentCode);

    @POST("api/Order/update-status")
    Call<ApiResponse<String>> updateOrderStatus(@Body UpdateOrderRequest request);

    @GET("api/Order/{id}")
    Call<ApiResponse<OrderResponse>> getOrderById(@Path("id") int orderId);
} 
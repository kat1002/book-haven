package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.request.CartItemUpdateRequest;
import com.son.bookhaven.data.dto.request.RemoveCartItemsRequest;
import com.son.bookhaven.data.dto.response.CartItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CartApiService {


    @GET("api/Cart/getCart")
    Call<List<CartItemResponse>> getCart();

    @POST("api/Cart/addItem")
    Call<Void> updateCartItemQuantity(@Body CartItemUpdateRequest request);

    @POST("api/Cart/removeItems")
    Call<Void> removeCartItems(@Body RemoveCartItemsRequest request);
}

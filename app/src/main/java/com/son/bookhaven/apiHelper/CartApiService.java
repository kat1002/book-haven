package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.CartItemResponse;
import com.son.bookhaven.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CartApiService {

    @GET("api/Cart/getUserCart")
    Call<List<CartItemResponse>> getUserCart(@Query("userId") int userId);
}

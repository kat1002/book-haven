package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.request.LoginRequest;
import com.son.bookhaven.data.dto.request.RegisterRequest;
import com.son.bookhaven.data.dto.response.LoginResponse;
import com.son.bookhaven.data.dto.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/register-mobile")
    Call<RegisterResponse> registerMobile(@Body RegisterRequest registerRequest);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);


}

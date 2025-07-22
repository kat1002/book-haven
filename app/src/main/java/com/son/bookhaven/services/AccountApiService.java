package com.son.bookhaven.services;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.ChangePasswordRequest;
import com.son.bookhaven.data.dto.request.ForgotPasswordRequest;
import com.son.bookhaven.data.dto.request.UserInfoUpdateRequest;
import com.son.bookhaven.data.dto.response.UpdateInfoResponse;
import com.son.bookhaven.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AccountApiService {
    @GET("api/Account/my-info")
    Call<User> getMyInfo();

    @PUT("api/account/update-info")
    Call<UpdateInfoResponse> updateUserInfo(@Body UserInfoUpdateRequest request);

    @POST("api/account/change-password")
    Call<ApiResponse> changePassword(
            @Header("Authorization") String authToken,
            @Body ChangePasswordRequest request
    );

    @POST("api/account/forgot-password")
    Call<ApiResponse> forgotPassword(
            @Body ForgotPasswordRequest request
    );
}

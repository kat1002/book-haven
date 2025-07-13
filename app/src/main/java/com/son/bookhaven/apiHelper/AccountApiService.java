package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.request.UserInfoUpdateRequest;
import com.son.bookhaven.data.dto.response.UpdateInfoResponse;
import com.son.bookhaven.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface AccountApiService {
    @GET("api/Account/my-info")
    Call<User> getMyInfo();

    @PUT("api/account/update-info")
    Call<UpdateInfoResponse> updateUserInfo(@Body UserInfoUpdateRequest request);
}

package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface AccountApiService {
    @GET("api/Account/my-info")
    Call<User> getMyInfo(@Header("Authorization") String authToken);
}

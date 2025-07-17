package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.PublisherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PublisherApiService {
    @GET("api/Publisher")
    Call<ApiResponse<List<PublisherResponse>>> getAllPublishers();
}

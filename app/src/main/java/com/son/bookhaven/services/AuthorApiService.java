package com.son.bookhaven.services;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.AuthorResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AuthorApiService {
    @GET("api/Author")
    Call<ApiResponse<List<AuthorResponse>>> getAllAuthors();
}

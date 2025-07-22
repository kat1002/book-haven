package com.son.bookhaven.services;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApiService {
    @GET("api/Category")
    Call<ApiResponse<List<CategoryResponse>>> getAllCategories();
}

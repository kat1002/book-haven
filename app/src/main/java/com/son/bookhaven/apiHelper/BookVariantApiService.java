package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.BookVariantResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookVariantApiService {
    @GET("api/BookVariant")
    Call<ApiResponse<List<BookVariantResponse>>> getAllVariants();

    @GET("api/BookVariant/{id}")
    Call<ApiResponse<BookVariantResponse>> getVariantById(@Path("id") int id);

    @GET("api/BookVariant/search")
    Call<ApiResponse<List<BookVariantResponse>>> searchVariants(@Query("variantTitle") String variantTitle);

    @GET("api/BookVariant/filter")
    Call<ApiResponse<List<BookVariantResponse>>> filterVariants(
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("authorId") Integer authorId,
            @Query("categoryId") Integer categoryId,
            @Query("publisherId") Integer publisherId,
            @Query("language") String language
    );

    @GET("variants/book/{bookId}")
    Call<ApiResponse<List<BookVariantResponse>>> getVariantsByBookId(@Path("bookId") String bookId);
}

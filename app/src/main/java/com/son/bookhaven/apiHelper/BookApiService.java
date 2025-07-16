package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.BookResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApiService {
    @GET("api/Book")
    Call<ApiResponse<List<BookResponse>>> getAllBooks();

    @GET("api/Book/{id}")
    Call<ApiResponse<BookResponse>> getBookById(@Path("id") int id);

    @GET("api/Book/search")
    Call<ApiResponse<List<BookResponse>>> searchBooks(@Query("bookTitle") String bookTitle);
}

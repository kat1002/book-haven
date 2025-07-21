package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.ValidVouchersResponse;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VoucherApiService {
    @GET("api/Voucher/valid-vouchers")
    Call<ApiResponse<ValidVouchersResponse>> getValidVouchers(@Query("orderTotal") BigDecimal orderTotal);
}
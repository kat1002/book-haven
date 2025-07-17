package com.son.bookhaven.apiHelper;

import com.son.bookhaven.data.dto.response.DistrictResponse;
import com.son.bookhaven.data.dto.response.ProvinceResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AddressApiService {
    @GET("api/p/")
    Call<List<ProvinceResponse>> getProvinces();

    @GET("api/p/{provinceCode}?depth=2")
    Call<ProvinceResponse> getDistricts(@Path("provinceCode") int provinceCode);

    @GET("api/d/{districtCode}?depth=2")
    Call<DistrictResponse> getWards(@Path("districtCode") int districtCode);
}
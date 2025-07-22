package com.son.bookhaven.utils;

import com.son.bookhaven.services.AddressApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddressApiClient {
    private static final String BASE_URL = "https://provinces.open-api.vn/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AddressApiService getAddressService() {
        return getClient().create(AddressApiService.class);
    }
}
package com.son.bookhaven.utils;

import android.content.Context;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://10.0.2.2:7181/"; // URL of the API

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create OkHttpClient with custom SSL handling
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(getUnsafeSSLContext().getSocketFactory(), getUnsafeTrustManager())
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    // Method to get authenticated client
    public static Retrofit getAuthenticatedClient(Context context) {
        // Create a token interceptor
        Interceptor tokenInterceptor = chain -> {
            Request originalRequest = chain.request();

            // Get token from TokenManager
            TokenManager tokenManager = new TokenManager(context);
            String token = tokenManager.getToken();

            // If token exists, add it to the header
            if (token != null && !token.isEmpty()) {
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();

                return chain.proceed(newRequest);
            }

            return chain.proceed(originalRequest);
        };

        // Build OkHttpClient with both the interceptor and SSL handling
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .sslSocketFactory(getUnsafeSSLContext().getSocketFactory(), getUnsafeTrustManager())
                .hostnameVerifier((hostname, session) -> true)
                .build();

        // Create and return a new Retrofit instance with the authenticated client
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static SSLContext getUnsafeSSLContext() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{getUnsafeTrustManager()}, new SecureRandom());
            return sc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager getUnsafeTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0]; // Return empty array instead of null
            }
        };
    }
}
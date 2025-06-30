package com.son.bookhaven.apiHelper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

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
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0]; // Return empty array instead of null
            }
        };
    }
}
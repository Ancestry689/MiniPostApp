package com.example.minipost.http;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 后端服务器的地址
    private static final String BASE_URL = "http://192.168.149.132:3000/"; // 本地测试地址

    // 单例模式：确保 Retrofit 实例唯一
    private static Retrofit retrofit;

    // 获取 Retrofit 实例
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

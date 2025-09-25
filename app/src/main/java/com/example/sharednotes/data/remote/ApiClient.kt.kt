package com.example.sharednotes.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.example.sharednotes.network.TokenInterceptor
import okhttp3.OkHttpClient


object ApiClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.200:8000/") // Android 模拟器访问本机用 10.0.2.2
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)





}
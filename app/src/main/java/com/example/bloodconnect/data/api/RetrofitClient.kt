package com.example.bloodconnect.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val FIREBASE_BASE_URL = "https://bloodconnect-c532f-default-rtdb.asia-southeast1.firebasedatabase.app/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FIREBASE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
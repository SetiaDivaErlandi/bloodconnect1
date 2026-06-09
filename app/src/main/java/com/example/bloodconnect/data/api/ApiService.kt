package com.example.bloodconnect.data.api

import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.UserResponse
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.data.model.Article
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 1. GitHub Raw Data Endpoints (Absolute URLs)
    @GET("https://raw.githubusercontent.com/SetiaDivaErlandi/bloodconnect-data/main/articles.json")
    suspend fun getBloodData(): BloodDataResponse

    @GET("https://raw.githubusercontent.com/SetiaDivaErlandi/bloodconnect-data/main/users.json")
    suspend fun getUsers(): List<UserResponse>

    // 2. Firebase Realtime Database Endpoints (Relative to Firebase Base URL)
    @GET("users.json")
    suspend fun getFirebaseUsers(): Map<String, UserResponse>?

    @PUT("users/{id}.json")
    suspend fun registerFirebaseUser(
        @Path("id") id: String,
        @Body user: UserResponse
    ): UserResponse

    @GET("donors.json")
    suspend fun getFirebaseDonors(): Map<String, Donor>?

    @PUT("donors/{id}.json")
    suspend fun saveFirebaseDonor(
        @Path("id") id: String,
        @Body donor: Donor
    ): Donor

    @GET("articles.json")
    suspend fun getFirebaseArticles(): Map<String, Article>?

    @PUT("articles/{id}.json")
    suspend fun saveFirebaseArticle(
        @Path("id") id: String,
        @Body article: Article
    ): Article
}

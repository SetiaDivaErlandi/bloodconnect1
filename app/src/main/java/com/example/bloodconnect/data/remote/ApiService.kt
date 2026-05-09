package com.example.bloodconnect.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface ApiService {

    @GET("articles.json")
    suspend fun getBloodData(): BloodDataResponse

    @GET("users.json")
    suspend fun getUsers(): List<UserResponse>

    companion object {
        // Base URL global yang rapi
        const val BASE_URL = "https://raw.githubusercontent.com/SetiaDivaErlandi/bloodconnect-data/main/"
    }
}

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("bloodType") val bloodType: String,
    @SerializedName("location") val location: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("phone") val phone: String
)

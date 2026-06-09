package com.example.bloodconnect.data.api

import com.example.bloodconnect.data.model.BloodDataResponse
import com.example.bloodconnect.data.model.UserResponse
import com.example.bloodconnect.data.model.Donor
import com.example.bloodconnect.data.model.Article
import com.example.bloodconnect.data.model.SosRequestResponse
import com.example.bloodconnect.data.model.DonationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("https://raw.githubusercontent.com/SetiaDivaErlandi/bloodconnect-data/main/articles.json")
    suspend fun getBloodData(): BloodDataResponse

    @GET("https://raw.githubusercontent.com/SetiaDivaErlandi/bloodconnect-data/main/users.json")
    suspend fun getUsers(): List<UserResponse>

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

    @GET("sos_requests.json")
    suspend fun getFirebaseSosRequests(): Map<String, SosRequestResponse>?

    @PUT("sos_requests/{id}.json")
    suspend fun saveFirebaseSosRequest(
        @Path("id") id: String,
        @Body request: SosRequestResponse
    ): SosRequestResponse

    @GET("donations/{userId}.json")
    suspend fun getFirebaseDonations(
        @Path("userId") userId: String
    ): Map<String, DonationResponse>?

    @PUT("donations/{userId}/{id}.json")
    suspend fun saveFirebaseDonation(
        @Path("userId") userId: String,
        @Path("id") id: String,
        @Body donation: DonationResponse
    ): DonationResponse
}

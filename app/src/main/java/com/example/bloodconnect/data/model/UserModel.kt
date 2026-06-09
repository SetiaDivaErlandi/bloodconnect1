package com.example.bloodconnect.data.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val bloodType: String,
    val location: String,
    val imageUrl: String,
    val phone: String,
    val password: String = ""
)

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

data class Donor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("bloodType") val bloodType: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("location") val location: String,
    @SerializedName("imageUrl") val imageUrl: String
)

data class Article(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("category") val category: String,
    @SerializedName("time") val time: String
)

data class BloodDataResponse(
    @SerializedName("donors") val donors: List<Donor>,
    @SerializedName("articles") val articles: List<Article>
)

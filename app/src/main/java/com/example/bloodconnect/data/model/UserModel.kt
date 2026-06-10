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
    val gender: String = "Laki-laki",
    val password: String = ""
)

data class UserResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("bloodType") val bloodType: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("gender") val gender: String? = "Laki-laki"
)

data class SosRequestResponse(
    @SerializedName("id") val id: String,
    @SerializedName("requesterName") val requesterName: String,
    @SerializedName("bloodType") val bloodType: String,
    @SerializedName("location") val location: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("notes") val notes: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("requesterPhone") val requesterPhone: String = "",
    @SerializedName("requesterId") val requesterId: String = ""
)

data class DonationResponse(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("hospital") val hospital: String,
    @SerializedName("status") val status: String,
    @SerializedName("isCompleted") val isCompleted: Boolean
)

data class Donor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("bloodType") val bloodType: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("location") val location: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("phone") val phone: String = "08123456789",
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
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

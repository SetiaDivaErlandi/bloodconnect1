package com.example.bloodconnect.data.repository

import com.example.bloodconnect.data.api.ApiService
import com.example.bloodconnect.data.model.UserModel
import com.example.bloodconnect.data.model.UserResponse

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): UserModel? {
        val usersMap = apiService.getFirebaseUsers() ?: return null
        val matchedResponse = usersMap.values.find { 
            it.email.trim().equals(email.trim(), ignoreCase = true) && 
            it.password.trim() == password.trim() 
        } ?: return null
        
        return UserModel(
            id = matchedResponse.id,
            name = matchedResponse.name,
            email = matchedResponse.email,
            bloodType = matchedResponse.bloodType,
            location = matchedResponse.location,
            imageUrl = matchedResponse.imageUrl,
            phone = matchedResponse.phone
        )
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        bloodType: String,
        location: String,
        gender: String
    ): UserModel {
        val id = "user_${System.currentTimeMillis()}"
        val avatarSeed = if (gender == "Perempuan") {
            val girls = listOf("Lily", "Aneka", "Mariah", "Sadie", "Zoe")
            girls[name.trim().length % girls.size]
        } else {
            val boys = listOf("Felix", "Jack", "Oliver", "Charlie", "George")
            boys[name.trim().length % boys.size]
        }
        val imageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$avatarSeed&eyes=default&mouth=smile&eyebrowType=default"
        val userResponse = UserResponse(
            id = id,
            name = name.trim(),
            email = email.trim(),
            password = password.trim(),
            bloodType = bloodType,
            location = location,
            imageUrl = imageUrl,
            phone = phone.trim(),
            gender = gender
        )
        apiService.registerFirebaseUser(id, userResponse)
        
        return UserModel(
            id = id,
            name = userResponse.name,
            email = userResponse.email,
            bloodType = userResponse.bloodType,
            location = userResponse.location,
            imageUrl = userResponse.imageUrl,
            phone = userResponse.phone,
            gender = userResponse.gender
        )
    }

    suspend fun updateProfile(
        id: String,
        name: String,
        phone: String,
        location: String,
        imageUrl: String
    ): UserModel? {
        val usersMap = apiService.getFirebaseUsers() ?: return null
        val existingResponse = usersMap[id] ?: usersMap.values.find { it.id == id } ?: return null
        
        val updatedResponse = existingResponse.copy(
            name = name.trim(),
            phone = phone.trim(),
            location = location.trim(),
            imageUrl = imageUrl
        )
        apiService.registerFirebaseUser(id, updatedResponse)
        
        return UserModel(
            id = id,
            name = updatedResponse.name,
            email = updatedResponse.email,
            bloodType = updatedResponse.bloodType,
            location = updatedResponse.location,
            imageUrl = updatedResponse.imageUrl,
            phone = updatedResponse.phone,
            gender = updatedResponse.gender
        )
    }
}

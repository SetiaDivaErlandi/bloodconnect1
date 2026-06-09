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
        location: String
    ): UserModel {
        val id = "user_${System.currentTimeMillis()}"
        val imageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$name"
        val userResponse = UserResponse(
            id = id,
            name = name.trim(),
            email = email.trim(),
            password = password.trim(),
            bloodType = bloodType,
            location = location,
            imageUrl = imageUrl,
            phone = phone.trim()
        )
        apiService.registerFirebaseUser(id, userResponse)
        
        return UserModel(
            id = id,
            name = userResponse.name,
            email = userResponse.email,
            bloodType = userResponse.bloodType,
            location = userResponse.location,
            imageUrl = userResponse.imageUrl,
            phone = userResponse.phone
        )
    }
}

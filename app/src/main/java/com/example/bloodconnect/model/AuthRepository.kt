package com.example.bloodconnect.model

import com.example.bloodconnect.model.api.ApiService
import com.example.bloodconnect.model.UserModel
import com.example.bloodconnect.model.UserResponse

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): UserModel? {
        val usersMap = apiService.getFirebaseUsers() ?: return null
        val matchedResponse = usersMap.values.find { 
            it.email?.trim().equals(email.trim(), ignoreCase = true) && 
            it.password?.trim() == password.trim() 
        } ?: return null
        
        return UserModel(
            id = matchedResponse.id ?: "",
            name = matchedResponse.name ?: "User",
            email = matchedResponse.email ?: "",
            bloodType = matchedResponse.bloodType ?: "-",
            location = matchedResponse.location ?: "",
            imageUrl = matchedResponse.imageUrl ?: "",
            phone = matchedResponse.phone ?: "",
            gender = matchedResponse.gender ?: "Laki-laki",
            password = matchedResponse.password ?: ""
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
            name = userResponse.name ?: name,
            email = userResponse.email ?: email,
            bloodType = userResponse.bloodType ?: bloodType,
            location = userResponse.location ?: location,
            imageUrl = userResponse.imageUrl ?: imageUrl,
            phone = userResponse.phone ?: phone,
            gender = userResponse.gender ?: gender,
            password = userResponse.password ?: password
        )
    }

    suspend fun updateProfile(
        id: String,
        name: String,
        phone: String,
        location: String,
        imageUrl: String,
        bloodType: String,
        gender: String
    ): UserModel? {
        val usersMap = apiService.getFirebaseUsers() ?: return null
        val existingResponse = usersMap[id] ?: usersMap.values.find { it.id == id } ?: return null
        
        val updatedResponse = existingResponse.copy(
            name = name.trim(),
            phone = phone.trim(),
            location = location.trim(),
            imageUrl = imageUrl,
            bloodType = bloodType,
            gender = gender
        )
        apiService.registerFirebaseUser(id, updatedResponse)
        
        return UserModel(
            id = id,
            name = updatedResponse.name ?: name,
            email = updatedResponse.email ?: "",
            bloodType = updatedResponse.bloodType ?: bloodType,
            location = updatedResponse.location ?: location,
            imageUrl = updatedResponse.imageUrl ?: imageUrl,
            phone = updatedResponse.phone ?: phone,
            gender = updatedResponse.gender ?: gender,
            password = updatedResponse.password ?: existingResponse.password ?: ""
        )
    }

    suspend fun getUserByEmailWithKey(email: String): Pair<String, UserResponse>? {
        val usersMap = apiService.getFirebaseUsers() ?: return null
        val entry = usersMap.entries.find { it.value.email?.trim().equals(email.trim(), ignoreCase = true) }
        return entry?.let { it.key to it.value }
    }

    suspend fun updatePasswordOnly(key: String, newPassword: String) {
        val updates = mapOf("password" to newPassword.trim())
        apiService.updateFirebaseUserFields(key, updates)
    }
}

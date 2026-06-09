package com.example.bloodconnect.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.bloodconnect.data.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")


class UserPreferences(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_BLOOD_TYPE = stringPreferencesKey("user_blood_type")
        private val USER_LOCATION = stringPreferencesKey("user_location")
        private val USER_IMAGE_URL = stringPreferencesKey("user_image_url")
        private val USER_PHONE = stringPreferencesKey("user_phone")
        
        // For local simulation of "registered" user
        private val REG_EMAIL = stringPreferencesKey("reg_email")
        private val REG_PASSWORD = stringPreferencesKey("reg_password")
        private val REG_NAME = stringPreferencesKey("reg_name")
        private val REG_PHONE = stringPreferencesKey("reg_phone")
        private val REG_BLOOD_TYPE = stringPreferencesKey("reg_blood_type")
        private val REG_LOCATION = stringPreferencesKey("reg_location")
    }

    val userData: Flow<UserModel?> = context.dataStore.data.map { preferences ->
        if (preferences[IS_LOGGED_IN] == true) {
            UserModel(
                id = preferences[USER_ID] ?: "",
                name = preferences[USER_NAME] ?: "",
                email = preferences[USER_EMAIL] ?: "",
                bloodType = preferences[USER_BLOOD_TYPE] ?: "",
                location = preferences[USER_LOCATION] ?: "",
                imageUrl = preferences[USER_IMAGE_URL] ?: "",
                phone = preferences[USER_PHONE] ?: ""
            )
        } else {
            null
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun saveLoginSession(user: UserModel) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_BLOOD_TYPE] = user.bloodType
            preferences[USER_LOCATION] = user.location
            preferences[USER_IMAGE_URL] = user.imageUrl
            preferences[USER_PHONE] = user.phone
        }
    }

    // Save registered user for local login simulation since we don't have a writable API
    suspend fun saveRegisteredUser(user: UserModel, password: String) {
        context.dataStore.edit { preferences ->
            preferences[REG_EMAIL] = user.email
            preferences[REG_PASSWORD] = password
            preferences[REG_NAME] = user.name
            preferences[REG_PHONE] = user.phone
            preferences[REG_BLOOD_TYPE] = user.bloodType
            preferences[REG_LOCATION] = user.location
        }
    }

    val registeredUser: Flow<Map<String, String>> = context.dataStore.data.map { preferences ->
        mapOf(
            "email" to (preferences[REG_EMAIL] ?: ""),
            "password" to (preferences[REG_PASSWORD] ?: ""),
            "name" to (preferences[REG_NAME] ?: ""),
            "phone" to (preferences[REG_PHONE] ?: ""),
            "bloodType" to (preferences[REG_BLOOD_TYPE] ?: ""),
            "location" to (preferences[REG_LOCATION] ?: "")
        )
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            // We keep the REG_ fields so the user can log back in
        }
    }
}

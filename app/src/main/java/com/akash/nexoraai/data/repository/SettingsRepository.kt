package com.akash.nexoraai.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val hapticKey = booleanPreferencesKey("haptic_feedback")
    private val voiceKey = booleanPreferencesKey("voice_responses")
    private val dataSavingKey = booleanPreferencesKey("data_saving")
    private val profileImageKey = stringPreferencesKey("profile_image")
    private val userNameKey = stringPreferencesKey("user_name")
    private val backgroundKey = stringPreferencesKey("app_background")

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { it[hapticKey] ?: true }
    val voiceResponses: Flow<Boolean> = context.dataStore.data.map { it[voiceKey] ?: true }
    val dataSaving: Flow<Boolean> = context.dataStore.data.map { it[dataSavingKey] ?: false }
    val profileImage: Flow<String?> = context.dataStore.data.map { it[profileImageKey] }
    val userName: Flow<String> = context.dataStore.data.map { it[userNameKey] ?: "Akash Ray" }
    val appBackground: Flow<String?> = context.dataStore.data.map { it[backgroundKey] }

    suspend fun updateHaptic(enabled: Boolean) { context.dataStore.edit { it[hapticKey] = enabled } }
    suspend fun updateVoice(enabled: Boolean) { context.dataStore.edit { it[voiceKey] = enabled } }
    suspend fun updateDataSaving(enabled: Boolean) { context.dataStore.edit { it[dataSavingKey] = enabled } }
    suspend fun updateProfileImage(uri: String) { context.dataStore.edit { it[profileImageKey] = uri } }
    suspend fun updateUserName(name: String) { context.dataStore.edit { it[userNameKey] = name } }
    suspend fun updateBackground(colorOrUri: String) { context.dataStore.edit { it[backgroundKey] = colorOrUri } }
}

package com.akash.nexoraai.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akash.nexoraai.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val hapticFeedback = repository.hapticFeedback.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val voiceResponses = repository.voiceResponses.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val dataSaving = repository.dataSaving.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val profileImage = repository.profileImage.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val userName = repository.userName.stateIn(viewModelScope, SharingStarted.Eagerly, "Akash Ray")
    val appBackground = repository.appBackground.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _notifications = MutableStateFlow(listOf(
        Notification("Welcome to Nexora!", "Start exploring your new AI assistant.", System.currentTimeMillis()),
        Notification("Voice Assistant Ready", "You can now use real-time voice detection.", System.currentTimeMillis() - 3600000)
    ))
    val notifications = _notifications.asStateFlow()

    fun updateHaptic(enabled: Boolean) = viewModelScope.launch { repository.updateHaptic(enabled) }
    fun updateVoice(enabled: Boolean) = viewModelScope.launch { repository.updateVoice(enabled) }
    fun updateDataSaving(enabled: Boolean) = viewModelScope.launch { repository.updateDataSaving(enabled) }
    fun updateProfileImage(uri: String) = viewModelScope.launch { repository.updateProfileImage(uri) }
    fun updateUserName(name: String) = viewModelScope.launch { repository.updateUserName(name) }
    fun updateBackground(value: String) = viewModelScope.launch { repository.updateBackground(value) }
    
    fun clearNotifications() { _notifications.value = emptyList() }
}

data class Notification(
    val title: String,
    val content: String,
    val timestamp: Long
)

package com.argus.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argus.app.ai.AIServiceFactory
import com.argus.app.ai.AIServiceInfo
import com.argus.app.utils.ApiKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AISettingsViewModelSimple(private val context: Context) {
    
    private val apiKeyManager = ApiKeyManager(context)
    private val aiServiceFactory = AIServiceFactory(context)
    
    private val _availableServices = MutableStateFlow(aiServiceFactory.getAvailableServices())
    val availableServices: StateFlow<List<AIServiceInfo>> = _availableServices.asStateFlow()
    
    private val _currentService = MutableStateFlow(aiServiceFactory.getCurrentServiceInfo())
    val currentService: StateFlow<AIServiceInfo> = _currentService.asStateFlow()
    
    private val _openAIKey = MutableStateFlow(apiKeyManager.getOpenAIKey() ?: "")
    val openAIKey: StateFlow<String> = _openAIKey.asStateFlow()
    
    private val _geminiKey = MutableStateFlow(apiKeyManager.getGeminiKey() ?: "")
    val geminiKey: StateFlow<String> = _geminiKey.asStateFlow()
    
    fun selectService(service: AIServiceInfo) {
        apiKeyManager.setSelectedService(service.id)
        _currentService.value = service
        refreshServices()
    }
    
    fun updateOpenAIKey(key: String) {
        _openAIKey.value = key
        if (key.isNotBlank()) {
            apiKeyManager.setOpenAIKey(key)
        } else {
            apiKeyManager.removeOpenAIKey()
        }
        refreshServices()
    }
    
    fun updateGeminiKey(key: String) {
        _geminiKey.value = key
        if (key.isNotBlank()) {
            apiKeyManager.setGeminiKey(key)
        } else {
            apiKeyManager.removeGeminiKey()
        }
        refreshServices()
    }
    
    private fun refreshServices() {
        _availableServices.value = aiServiceFactory.getAvailableServices()
        _currentService.value = aiServiceFactory.getCurrentServiceInfo()
    }
}
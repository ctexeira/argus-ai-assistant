package com.argus.app.ai

import android.content.Context
import com.argus.app.utils.ApiKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AISettingsViewModelSimple(private val context: Context) {
    private val aiServiceFactory = AIServiceFactory(context)
    
    private val _currentService = MutableStateFlow<AIServiceInfo?>(null)
    val currentService: StateFlow<AIServiceInfo?> = _currentService.asStateFlow()
    
    private val _availableServices = MutableStateFlow<List<AIServiceInfo>>(emptyList())
    val availableServices: StateFlow<List<AIServiceInfo>> = _availableServices.asStateFlow()
    
    private val _openAIKey = MutableStateFlow("")
    val openAIKey: StateFlow<String> = _openAIKey.asStateFlow()
    
    private val _geminiKey = MutableStateFlow("")
    val geminiKey: StateFlow<String> = _geminiKey.asStateFlow()
    
    init {
        loadServices()
        loadApiKeys()
    }
    
    private fun loadServices() {
        _availableServices.value = aiServiceFactory.getAvailableServices()
        _currentService.value = aiServiceFactory.getCurrentServiceInfo()
    }
    
    private fun loadApiKeys() {
        val apiKeyManager = ApiKeyManager(context)
        _openAIKey.value = apiKeyManager.getOpenAIKey() ?: ""
        _geminiKey.value = apiKeyManager.getGeminiKey() ?: ""
    }
    
    fun selectService(serviceId: String) {
        val apiKeyManager = ApiKeyManager(context)
        apiKeyManager.setSelectedService(serviceId)
        _currentService.value = aiServiceFactory.getAvailableServices().find { it.id == serviceId }
    }
    
    fun updateOpenAIKey(key: String) {
        _openAIKey.value = key
        val apiKeyManager = ApiKeyManager(context)
        apiKeyManager.setOpenAIKey(key)
    }
    
    fun updateGeminiKey(key: String) {
        _geminiKey.value = key
        val apiKeyManager = ApiKeyManager(context)
        apiKeyManager.setGeminiKey(key)
    }
}
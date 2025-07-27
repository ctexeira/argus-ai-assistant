package com.argus.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyManager(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "argus_api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val OPENAI_API_KEY = "openai_api_key"
        private const val GEMINI_API_KEY = "gemini_api_key"
        private const val SELECTED_AI_SERVICE = "selected_ai_service"
        
        const val SERVICE_LOCAL = "local"
        const val SERVICE_OPENAI = "openai"
        const val SERVICE_GEMINI = "gemini"
    }
    
    fun setOpenAIKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(OPENAI_API_KEY, apiKey)
            .apply()
    }
    
    fun getOpenAIKey(): String? {
        return encryptedPrefs.getString(OPENAI_API_KEY, null)
    }
    
    fun setGeminiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString(GEMINI_API_KEY, apiKey)
            .apply()
    }
    
    fun getGeminiKey(): String? {
        return encryptedPrefs.getString(GEMINI_API_KEY, null)
    }
    
    fun setSelectedService(service: String) {
        encryptedPrefs.edit()
            .putString(SELECTED_AI_SERVICE, service)
            .apply()
    }
    
    fun getSelectedService(): String {
        return encryptedPrefs.getString(SELECTED_AI_SERVICE, SERVICE_LOCAL) ?: SERVICE_LOCAL
    }
    
    fun hasOpenAIKey(): Boolean {
        return !getOpenAIKey().isNullOrBlank()
    }
    
    fun hasGeminiKey(): Boolean {
        return !getGeminiKey().isNullOrBlank()
    }
    
    fun clearAllKeys() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
    
    fun removeOpenAIKey() {
        encryptedPrefs.edit()
            .remove(OPENAI_API_KEY)
            .apply()
    }
    
    fun removeGeminiKey() {
        encryptedPrefs.edit()
            .remove(GEMINI_API_KEY)
            .apply()
    }
    
    // Debug method to set service to Gemini if it has a key
    fun ensureGeminiServiceIfAvailable() {
        if (hasGeminiKey() && getSelectedService() != SERVICE_GEMINI) {
            setSelectedService(SERVICE_GEMINI)
        }
    }
}
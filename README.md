# Argus AI Assistant

An Android application that provides a floating AI chat assistant with system-wide overlay support. The app integrates with OpenAI GPT and Google Gemini APIs to provide intelligent conversational assistance.

## Features

- **Floating Chat Interface**: System-wide overlay that can be accessed from any app
- **Multiple AI Providers**: Support for OpenAI GPT and Google Gemini APIs
- **Local Fallback**: Works without API keys using a local service
- **Enhanced Error Handling**: Specific error messages for API key issues, rate limits, and server errors
- **Secure API Key Storage**: Encrypted storage of API keys using Android's EncryptedSharedPreferences
- **Permission Management**: Automatic handling of overlay and audio recording permissions

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/ctexeira/argus-ai-assistant.git
   cd argus-ai-assistant
   ```

2. Open the project in Android Studio

3. Build and run the project:
   ```bash
   ./gradlew assembleDebug
   ```

## Configuration

### API Keys (Optional)

To use OpenAI or Gemini services, configure your API keys in the app settings:

1. Open the app
2. Go to Settings
3. Navigate to AI Settings
4. Enter your OpenAI and/or Gemini API keys

### Permissions

The app requires the following permissions:
- **System Alert Window**: For floating overlay functionality
- **Record Audio**: For voice input (if implemented)

## Architecture

- **MVVM Pattern**: Uses ViewModel and Compose for UI
- **Dependency Injection**: Manual DI with factory pattern
- **Service Architecture**: Floating chat runs as a foreground service
- **Secure Storage**: API keys stored using EncryptedSharedPreferences

## Key Components

- `FloatingChatService`: Manages the floating overlay window
- `AIServiceFactory`: Creates appropriate AI service instances
- `OpenAIService` & `GeminiService`: Handle API communications
- `ApiKeyManager`: Secure storage and retrieval of API keys
- `PermissionManager`: Handles system permissions

## Recent Improvements

- Fixed floating assistant toggle state persistence
- Enhanced error handling with specific API error messages
- Improved permission state refresh when returning from settings
- Added comprehensive .gitignore for Android projects

## Build Requirements

- Android Studio Arctic Fox or later
- Android SDK 24+ (API level 24)
- Kotlin 1.8+
- Gradle 8.0+

## License

This project is open source. Please check the license file for details.
package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Models for Gemini API ---

data class Part(
    @Json(name = "text") val text: String? = null
)

data class Content(
    @Json(name = "role") val role: String? = null, // "user" or "model"
    @Json(name = "parts") val parts: List<Part>
)

data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class Candidate(
    @Json(name = "content") val content: Content?,
    @Json(name = "finishReason") val finishReason: String? = null
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit client singleton ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Executes content generation through the Gemini REST endpoint.
     */
    suspend fun fetchAiResponse(
        prompt: String,
        history: List<Content> = emptyList(),
        useProModel: Boolean = false,
        systemInstruction: String? = null
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY_DEFAULT_VALUE") {
            return "Error: Gemini API Key is missing. Please configure it in your Secrets panel inside Google AI Studio."
        }

        val model = if (useProModel) "gemini-3.1-pro-preview" else "gemini-3.5-flash"

        // Build conversational contents list
        val contentsList = mutableListOf<Content>()
        contentsList.addAll(history)
        contentsList.add(Content(role = "user", parts = listOf(Part(text = prompt))))

        val sysInstructionContent = systemInstruction?.let {
            Content(parts = listOf(Part(text = it)))
        }

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = sysInstructionContent
        )

        return try {
            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Error: Empty response received from SOLE AI model."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error contacting SOLE AI engine: ${e.message ?: "Unknown Connection Issue"}"
        }
    }
}

package com.studyflow.tracker.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CanvasOAuthService(private val context: Context) {
    
    companion object {
        private const val REDIRECT_URI = "studyflow://oauth/canvas"
        private const val SCOPE = "url:GET|/api/v1/courses url:GET|/api/v1/courses/*/assignments url:GET|/api/v1/users/self"
    }
    
    private val canvasService = CanvasService(context)
    
    /**
     * Initiates OAuth flow for Canvas LMS
     * Opens browser for user authentication
     */
    fun startCanvasOAuth(canvasUrl: String) {
        if (canvasUrl.isBlank()) return
        
        val clientId = "studyflow_android" // This would be registered with Canvas instance
        val state = generateRandomState()
        
        // Store state for verification
        val prefs = context.getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("oauth_state", state).apply()
        
        val authUrl = buildString {
            append("$canvasUrl/login/oauth2/auth")
            append("?client_id=${URLEncoder.encode(clientId, "UTF-8")}")
            append("&response_type=code")
            append("&redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}")
            append("&scope=${URLEncoder.encode(SCOPE, "UTF-8")}")
            append("&state=${URLEncoder.encode(state, "UTF-8")}")
        }
        
        // Open Custom Tab for OAuth
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        
        customTabsIntent.launchUrl(context, Uri.parse(authUrl))
    }
    
    /**
     * Handles OAuth callback and exchanges code for access token
     */
    suspend fun handleOAuthCallback(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            
            if (code.isNullOrBlank() || state.isNullOrBlank()) {
                return@withContext false
            }
            
            // Verify state
            val prefs = context.getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
            val storedState = prefs.getString("oauth_state", "")
            if (state != storedState) {
                return@withContext false
            }
            
            // Exchange code for token
            val accessToken = exchangeCodeForToken(code)
            if (accessToken != null) {
                canvasService.canvasToken = accessToken
                
                // Test connection and get user info
                val userInfo = getUserInfo()
                if (userInfo != null) {
                    // Store user info
                    prefs.edit()
                        .putString("canvas_user_name", userInfo.first)
                        .putString("canvas_user_email", userInfo.second)
                        .apply()
                    
                    canvasService.isConnected = true
                    return@withContext true
                }
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun exchangeCodeForToken(code: String): String? = withContext(Dispatchers.IO) {
        try {
            val canvasUrl = canvasService.canvasUrl
            if (canvasUrl.isBlank()) return@withContext null
            
            val tokenUrl = "$canvasUrl/login/oauth2/token"
            val url = URL(tokenUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true
            
            val postData = buildString {
                append("grant_type=authorization_code")
                append("&client_id=studyflow_android")
                append("&client_secret=") // Would be securely stored/retrieved
                append("&redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}")
                append("&code=${URLEncoder.encode(code, "UTF-8")}")
            }
            
            connection.outputStream.use { it.write(postData.toByteArray()) }
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                return@withContext jsonResponse.getString("access_token")
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun getUserInfo(): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val canvasUrl = canvasService.canvasUrl
            val token = canvasService.canvasToken
            
            if (canvasUrl.isBlank() || token.isBlank()) return@withContext null
            
            val url = URL("$canvasUrl/api/v1/users/self")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                
                val name = jsonResponse.getString("name")
                val email = jsonResponse.optString("email", "")
                
                return@withContext Pair(name, email)
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateRandomState(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Logout and clear OAuth tokens
     */
    fun logout() {
        canvasService.canvasToken = ""
        canvasService.isConnected = false
        
        val prefs = context.getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("canvas_user_name")
            .remove("canvas_user_email")
            .remove("oauth_state")
            .apply()
    }
}

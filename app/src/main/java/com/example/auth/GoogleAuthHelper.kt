package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GoogleUserData(
    val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val idToken: String? = null
)

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    /**
     * Retrieves the configured Web Client ID from resources or environment.
     * Returns null if not configured or if it's a placeholder.
     */
    fun getConfiguredClientId(context: Context): String? {
        // 1. Check if defined in resources (e.g. from google-services.json or strings.xml)
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val resValue = context.getString(resId).trim()
            if (isValidClientId(resValue)) {
                return resValue
            }
        }
        return null
    }

    /**
     * Checks whether Google Sign-In is configured with a valid OAuth 2.0 Client ID.
     */
    fun isConfigured(context: Context): Boolean {
        return getConfiguredClientId(context) != null
    }

    private fun isValidClientId(clientId: String?): Boolean {
        if (clientId.isNullOrBlank()) return false
        if (clientId.contains("mock", ignoreCase = true) || clientId.contains("placeholder", ignoreCase = true)) return false
        return clientId.endsWith(".apps.googleusercontent.com")
    }

    suspend fun signInWithGoogle(context: Context): Result<GoogleUserData> = withContext(Dispatchers.IO) {
        val clientId = getConfiguredClientId(context)
        if (clientId == null) {
            val errorMsg = "Google Sign-In is not configured. Add your OAuth 2.0 Web Client ID in Google Cloud Console / google-services.json to enable cloud authentication, or continue offline."
            Log.w(TAG, errorMsg)
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val userData = GoogleUserData(
                    userId = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User",
                    email = googleIdTokenCredential.id,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )
                return@withContext Result.success(userData)
            } else {
                return@withContext Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-in flow")
            return@withContext Result.failure(Exception("Sign-in was cancelled by user."))
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available on device: ${e.message}")
            return@withContext Result.failure(Exception("No Google account found on device."))
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager sign in failed: ${e.message}")
            return@withContext Result.failure(Exception("Google Sign-In failed: ${e.message}"))
        } catch (e: Exception) {
            Log.w(TAG, "Google sign in error: ${e.message}")
            return@withContext Result.failure(e)
        }
    }
}

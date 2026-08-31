package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object AuthHelper {
    suspend fun signInWithGoogle(context: Context): Boolean {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) return true // Already signed in

        val credentialManager = CredentialManager.create(context)
        
        // Note: For a real production app, the web client ID should come from string resources or secrets.
        val clientIdString = context.getString(com.example.R.string.web_client_id)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientIdString)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            Log.e("AuthHelper", "Google Sign-In failed", e)
            false
        } catch (e: Exception) {
             Log.e("AuthHelper", "Unknown error", e)
             false
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            return try {
                val authCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                true
            } catch (e: Exception) {
                Log.e("AuthHelper", "Firebase Auth failed", e)
                false
            }
        }
        return false
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}

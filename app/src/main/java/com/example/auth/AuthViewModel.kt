package com.example.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _isUserSignedIn = MutableStateFlow(auth.currentUser != null)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        auth.addAuthStateListener { firebaseAuth ->
            _isUserSignedIn.value = firebaseAuth.currentUser != null
        }
    }

    fun signIn(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            AuthHelper.signInWithGoogle(context)
            _isLoading.value = false
        }
    }

    fun signOut() {
        AuthHelper.signOut()
    }
}

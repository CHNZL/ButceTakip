package com.example.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userName: String, val userEmail: String, val idToken: String, val profilePictureUrl: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Ensure the client ID is present (not exactly the raw placeholder)
                val clientId = BuildConfig.WEB_CLIENT_ID
                if (clientId.isEmpty() || clientId.contains("your_web_client_id_here")) {
                    _authState.value = AuthState.Error("Lütfen AI Studio'da WEB_CLIENT_ID sırrını yapılandırın.")
                    return@launch
                }

                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(true)
                    .build()
                
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                
                val credential = result.credential
                
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val name = googleIdTokenCredential.displayName ?: "Gizli Kullanıcı"
                    val email = googleIdTokenCredential.id ?: "bilinmeyen_email"
                    val profilePic = googleIdTokenCredential.profilePictureUri?.toString()
                    var finalPic = profilePic
                    if (finalPic.isNullOrBlank()) {
                        try {
                            val parts = googleIdTokenCredential.idToken.split(".")
                            if (parts.size == 3) {
                                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                                val json = org.json.JSONObject(payload)
                                if (json.has("picture")) {
                                    finalPic = json.getString("picture")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    _authState.value = AuthState.Authenticated(name, email, googleIdTokenCredential.idToken, finalPic)
                    
                    // You could inject this token into Firebase Auth here if Firebase was setup.
                } else {
                    _authState.value = AuthState.Error("Bilinmeyen kimlik doğrulama yöntemi döndürüldü.")
                }
                
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Giriş iptal edildi veya başarısız oldu: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Beklenmeyen hata: ${e.message}")
            }
        }
    }

    fun signOut() {
        _authState.value = AuthState.Idle
    }
}

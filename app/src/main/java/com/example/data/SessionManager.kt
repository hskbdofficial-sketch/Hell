package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("hellsec_session_prefs", Context.MODE_PRIVATE)

    private val decryptedEmail: String? = prefs.getString("logged_in_email", null)?.let {
        CryptoUtils.decrypt(it)
    }

    private val _currentUserEmail = MutableStateFlow<String?>(decryptedEmail)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    fun login(email: String) {
        val encrypted = CryptoUtils.encrypt(email)
        prefs.edit().putString("logged_in_email", encrypted).apply()
        _currentUserEmail.value = email
    }

    fun logout() {
        prefs.edit().remove("logged_in_email").apply()
        _currentUserEmail.value = null
    }

    fun isLoggedIn(): Boolean {
        return _currentUserEmail.value != null
    }

    fun getLoggedInEmail(): String? {
        return _currentUserEmail.value
    }
}

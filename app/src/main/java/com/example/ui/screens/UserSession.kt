package com.example.ui.screens

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object UserSession {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_UID = "user_uid"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private var cachedUid: String = ""

    fun getUid(context: Context): String {
        if (cachedUid.isNotEmpty()) return cachedUid
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cachedUid = prefs.getString(KEY_UID, "") ?: ""
        if (cachedUid.isEmpty()) {
            cachedUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        }
        return cachedUid
    }

    fun saveSession(context: Context, uid: String) {
        cachedUid = uid
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_UID, uid)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun clearSession(context: Context) {
        cachedUid = ""
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val hasUid = (prefs.getString(KEY_UID, "") ?: "").isNotEmpty()
        val firebaseLoggedIn = FirebaseAuth.getInstance().currentUser != null
        return (prefLoggedIn && hasUid) || firebaseLoggedIn
    }
}

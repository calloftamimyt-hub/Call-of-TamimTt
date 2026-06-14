package com.example

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.screens.AppNotification
import com.example.ui.screens.NotificationType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object LocalNotificationManager {
    private const val PREFS_NAME = "local_notifications_prefs"
    private const val KEY_NOTIFICATIONS_PREFIX = "notifications_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun addNotification(
        context: Context,
        userId: String,
        title: String,
        message: String,
        type: NotificationType
    ): String {
        if (userId.isBlank()) return ""
        val prefs = getPrefs(context)
        val key = KEY_NOTIFICATIONS_PREFIX + userId
        val rawJson = prefs.getString(key, "[]") ?: "[]"
        
        val jsonArray = try {
            JSONArray(rawJson)
        } catch (e: Exception) {
            JSONArray()
        }

        val id = System.currentTimeMillis().toString() + "_" + UUID.randomUUID().toString().take(4)
        val timestamp = System.currentTimeMillis()
        
        val newObj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("message", message)
            put("timestamp", timestamp)
            put("type", type.name)
            put("isRead", false)
        }

        // Add to the front so newest is first
        val newArray = JSONArray()
        newArray.put(newObj)
        for (i in 0 until jsonArray.length()) {
            newArray.put(jsonArray.get(i))
        }

        prefs.edit().putString(key, newArray.toString()).apply()
        return id
    }

    @Synchronized
    fun getNotifications(context: Context, userId: String): List<AppNotification> {
        if (userId.isBlank()) return emptyList()
        val prefs = getPrefs(context)
        val key = KEY_NOTIFICATIONS_PREFIX + userId
        val rawJson = prefs.getString(key, "[]") ?: "[]"
        
        val list = mutableListOf<AppNotification>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val message = obj.getString("message")
                val timestamp = obj.getLong("timestamp")
                val typeStr = obj.getString("type")
                val isRead = obj.getBoolean("isRead")

                val type = try {
                    NotificationType.valueOf(typeStr)
                } catch (e: Exception) {
                    NotificationType.INFO
                }

                // Format friendly relative time
                val diff = System.currentTimeMillis() - timestamp
                val mins = diff / (60 * 1000)
                val timeText = if (mins < 1) {
                    "Just now"
                } else if (mins < 60) {
                    "$mins mins ago"
                } else {
                    val hours = mins / 60
                    if (hours < 24) {
                        "$hours hours ago"
                    } else {
                        "${hours / 24} days ago"
                    }
                }

                list.add(AppNotification(id, title, message, timeText, type, isRead))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @Synchronized
    fun markAsRead(context: Context, userId: String, notifId: String) {
        if (userId.isBlank()) return
        val prefs = getPrefs(context)
        val key = KEY_NOTIFICATIONS_PREFIX + userId
        val rawJson = prefs.getString(key, "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == notifId) {
                    obj.put("isRead", true)
                    break
                }
            }
            prefs.edit().putString(key, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun markAllAsRead(context: Context, userId: String) {
        if (userId.isBlank()) return
        val prefs = getPrefs(context)
        val key = KEY_NOTIFICATIONS_PREFIX + userId
        val rawJson = prefs.getString(key, "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                obj.put("isRead", true)
            }
            prefs.edit().putString(key, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

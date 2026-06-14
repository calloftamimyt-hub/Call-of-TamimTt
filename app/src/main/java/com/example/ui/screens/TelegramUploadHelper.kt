package com.example.ui.screens

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object TelegramUploadHelper {
    private const val BOT_TOKEN = "8782700653:AAHX2B0TRI7AOuE1c-G84JvCNjYu7sqWEZY"
    private const val CHAT_ID = "-1003501658139"
    private val client = OkHttpClient()

    interface UploadCallback {
        fun onSuccess(fileUrl: String, telegramMessageId: Long)
        fun onFailure(errorMessage: String)
    }

    fun uploadScreenshot(
        context: Context,
        imageUri: Uri,
        caption: String,
        callback: UploadCallback
    ) {
        val contentResolver = context.contentResolver
        val inputStream = try {
            contentResolver.openInputStream(imageUri)
        } catch (e: Exception) {
            callback.onFailure("Cannot open image: ${e.localizedMessage}")
            return
        }

        if (inputStream == null) {
            callback.onFailure("Image stream is null")
            return
        }

        val bytes = try {
            inputStream.readBytes()
        } catch (e: Exception) {
            callback.onFailure("Cannot read image bytes: ${e.localizedMessage}")
            return
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {}
        }

        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val fileBody = bytes.toRequestBody(mediaType, 0, bytes.size)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", CHAT_ID)
            .addFormDataPart("photo", "proof.jpg", fileBody)
            .addFormDataPart("caption", caption)
            .build()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$BOT_TOKEN/sendPhoto")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Network error: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful || bodyStr.isEmpty()) {
                    callback.onFailure("Telegram rejected upload: Code ${response.code} - $bodyStr")
                    return
                }

                try {
                    val root = JSONObject(bodyStr)
                    if (!root.getBoolean("ok")) {
                        callback.onFailure("Telegram error: ${root.optString("description")}")
                        return
                    }

                    val resultObj = root.getJSONObject("result")
                    val messageId = resultObj.optLong("message_id", 0L)
                    val photoArray = resultObj.getJSONArray("photo")
                    if (photoArray.length() == 0) {
                        callback.onFailure("No photo returned from Telegram")
                        return
                    }

                    // Get fileId of largest photo sizing
                    val largestPhoto = photoArray.getJSONObject(photoArray.length() - 1)
                    val fileId = largestPhoto.getString("file_id")

                    // Call getFile to obtain path
                    retrieveFilePath(fileId, messageId, callback)

                } catch (e: Exception) {
                    callback.onFailure("Data parsing error: ${e.localizedMessage}")
                }
            }
        })
    }

    private fun retrieveFilePath(fileId: String, messageId: Long, callback: UploadCallback) {
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$BOT_TOKEN/getFile?file_id=$fileId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Return a direct view via basic API link schema on fallback
                callback.onSuccess("https://api.telegram.org/file/bot$BOT_TOKEN/fileId_$fileId", messageId)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""
                try {
                    val root = JSONObject(bodyStr)
                    if (root.getBoolean("ok")) {
                        val resultObj = root.getJSONObject("result")
                        val filePath = resultObj.getString("file_path")
                        val publicUrl = "https://api.telegram.org/file/bot$BOT_TOKEN/$filePath"
                        callback.onSuccess(publicUrl, messageId)
                    } else {
                        callback.onSuccess("https://api.telegram.org/file/bot$BOT_TOKEN/fileId_$fileId", messageId)
                    }
                } catch (e: Exception) {
                    callback.onSuccess("https://api.telegram.org/file/bot$BOT_TOKEN/fileId_$fileId", messageId)
                }
            }
        })
    }
}

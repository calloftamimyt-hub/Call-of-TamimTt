package com.example

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object RechargeApiHelper {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("x-api-key", "mySuperSecureAppKey898001902319tuliTamim")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .build()

    class RechargeResult(
        val isSuccess: Boolean,
        val message: String,
        val transactionId: String? = null
    )

    suspend fun performRecharge(
        phone: String,
        amount: Double,
        operator: String
    ): RechargeResult = withContext(Dispatchers.IO) {
        try {
            var baseUrl = "https://recharge-api-backend.onrender.com"
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }
            val url = "$baseUrl/api/recharge"
            
            val json = JSONObject().apply {
                put("phone", phone)
                put("amount", amount)
                put("operator", operator)
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = json.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("RechargeApiHelper", "Response Code: ${response.code}, Body: $responseBody")
                
                if (response.isSuccessful) {
                    try {
                        val jsonObj = JSONObject(responseBody)
                        val status = jsonObj.optString("status", "")
                        val message = jsonObj.optString("message", "Recharge successful")
                        val txnId = jsonObj.optString("transaction_id", "")
                        RechargeResult(
                            isSuccess = (status == "success" || status == "approved"),
                            message = message,
                            transactionId = txnId
                        )
                    } catch (e: Exception) {
                        RechargeResult(isSuccess = false, message = "Invalid server response (not JSON format). Please check your RENDER_BASE_URL in AI Studio Secrets.")
                    }
                } else {
                    val jsonObj = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        null
                    }
                    val errorMsg = jsonObj?.optString("error") ?: "Server error: ${response.code} (Possible wrong URL or Server configuration)"
                    RechargeResult(isSuccess = false, message = errorMsg)
                }
            }
        } catch (e: IOException) {
            Log.e("RechargeApiHelper", "Network Error", e)
            RechargeResult(isSuccess = false, message = "Network error: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e("RechargeApiHelper", "Unexpected Error", e)
            RechargeResult(isSuccess = false, message = "Error: ${e.localizedMessage}")
        }
    }
}

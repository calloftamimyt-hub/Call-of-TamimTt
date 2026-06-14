package com.example.utils

import com.google.firebase.firestore.FirebaseFirestore

object ReferralCommissionHelper {
    fun applyCommission(userId: String, taskIncome: Double) {
        if (taskIncome <= 0.0) return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(userId).get().addOnSuccessListener { userSnap ->
            val referrerUid = userSnap.getString("referrerUid")
            if (!referrerUid.isNullOrEmpty()) {
                db.collection("settings").document("referral").get().addOnSuccessListener { settingsSnap ->
                    val isEnabled = settingsSnap.getBoolean("is_enabled") ?: true
                    if (!isEnabled) return@addOnSuccessListener
                    val commissionPercent = settingsSnap.getDouble("task_commission") ?: 0.0
                    if (commissionPercent <= 0.0) return@addOnSuccessListener
                    
                    val commissionAmount = (taskIncome * commissionPercent) / 100.0
                    val referrerRef = db.collection("users").document(referrerUid)
                    
                    db.runTransaction { tx ->
                        val refSnap = tx.get(referrerRef)
                        val refBalance = refSnap.getDouble("balance") ?: 0.0
                        tx.update(referrerRef, "balance", refBalance + commissionAmount)
                    }
                }
            }
        }
    }
}

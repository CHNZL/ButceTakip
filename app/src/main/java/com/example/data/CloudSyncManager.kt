package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CloudSyncManager {
    private var firestore: FirebaseFirestore? = null
    var isConfigured = false
        private set

    fun initialize(context: Context): Boolean {
        return try {
            val projectId = BuildConfig.FIREBASE_PROJECT_ID
            val appId = BuildConfig.FIREBASE_APP_ID
            val apiKey = BuildConfig.FIREBASE_API_KEY
            
            if (projectId == "your_firebase_project_id_here" || appId == "your_firebase_app_id_here" || apiKey == "your_firebase_api_key_here") {
                Log.w("CloudSync", "Firebase bilgileri girilmemiş. Senkronizasyon kapalı.")
                isConfigured = false
                return false
            }
            
            val options = FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .build()
            
            val appName = "CloudSyncApp_${projectId}"
            val app = try {
                FirebaseApp.getInstance(appName)
            } catch (e: IllegalStateException) {
                FirebaseApp.initializeApp(context.applicationContext, options, appName)
            }
            
            firestore = FirebaseFirestore.getInstance(app)
            isConfigured = true
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Firebase başlatılırken hata", e)
            isConfigured = false
            false
        }
    }

    suspend fun backupData(userId: String, data: Map<String, Any>): Boolean {
        val db = firestore ?: return false
        return try {
            if(userId.isBlank()) return false
            db.collection("users").document(userId).set(data).await()
            true
        } catch (e: Exception) {
            Log.e("CloudSync", "Yedekleme hatası", e)
            false
        }
    }

    suspend fun restoreData(userId: String): Map<String, Any>? {
        val db = firestore ?: return null
        return try {
            if(userId.isBlank()) return null
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.data
        } catch (e: Exception) {
            Log.e("CloudSync", "Geri yükleme hatası", e)
            null
        }
    }
}

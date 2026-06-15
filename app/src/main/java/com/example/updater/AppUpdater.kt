package com.example.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

class AppUpdater(private val context: Context) {

    // You can host a JSON file like this on GitHub Gist, Firebase Storage, or your own server.
    // Ensure the JSON response format matches:
    // {"versionCode": 2, "versionName": "1.1", "downloadUrl": "https://...", "releaseNotes": "..."}
    private val UPDATE_JSON_URL = "https://raw.githubusercontent.com/cihanozel10/BudgeTrackerApp/main/version.json" // Placeholder URL, user should replace

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(UPDATE_JSON_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val remoteVersionCode = json.optInt("versionCode", -1)
                val remoteVersionName = json.optString("versionName", "")
                val downloadUrl = json.optString("downloadUrl", "")
                val releaseNotes = json.optString("releaseNotes", "")

                if (remoteVersionCode > BuildConfig.VERSION_CODE && downloadUrl.isNotEmpty()) {
                    return@withContext UpdateInfo(remoteVersionCode, remoteVersionName, downloadUrl, releaseNotes)
                }
            }
        } catch (e: Exception) {
            Log.e("AppUpdater", "Error checking for update", e)
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(updateInfo: UpdateInfo) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(updateInfo.downloadUrl)
        
        val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }

        val request = DownloadManager.Request(uri)
            .setTitle("Uygulama Güncellemesi")
            .setDescription("Yeni sürüm indiriliyor...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(apkFile)
                    try {
                        context.unregisterReceiver(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppUpdater", "Error installing APK", e)
        }
    }
}

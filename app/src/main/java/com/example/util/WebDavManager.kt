package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class WebDavConfig(
    val serverUrl: String = "", // e.g. https://dav.jianguoyun.com/dav/StoragePro/
    val username: String = "",
    val password: String = "",
    val autoSync: Boolean = false,
    val lastSyncTime: Long = 0L
)

object WebDavManager {
    private const val PREFS_NAME = "storage_webdav_prefs"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun loadConfig(context: Context): WebDavConfig {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WebDavConfig(
            serverUrl = sp.getString("server_url", "") ?: "",
            username = sp.getString("username", "") ?: "",
            password = sp.getString("password", "") ?: "",
            autoSync = sp.getBoolean("auto_sync", false),
            lastSyncTime = sp.getLong("last_sync_time", 0L)
        )
    }

    fun saveConfig(context: Context, config: WebDavConfig) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString("server_url", config.serverUrl)
            .putString("username", config.username)
            .putString("password", config.password)
            .putBoolean("auto_sync", config.autoSync)
            .putLong("last_sync_time", config.lastSyncTime)
            .apply()
    }

    suspend fun testConnection(config: WebDavConfig): Result<String> = withContext(Dispatchers.IO) {
        if (config.serverUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请填写完整的 WebDAV 服务器地址与账号"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            val request = Request.Builder()
                .url(config.serverUrl)
                .addHeader("Authorization", credential)
                .method("PROPFIND", "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:allprop/></D:propfind>".toRequestBody("application/xml".toMediaType()))
                .addHeader("Depth", "0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 207 || response.code == 200 || response.code == 404) {
                    Result.success("WebDAV 连接测试成功！状态码: ${response.code}")
                } else {
                    Result.failure(Exception("连接失败: HTTP ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接异常: ${e.localizedMessage}"))
        }
    }

    suspend fun uploadBackup(context: Context, config: WebDavConfig, backupJson: String): Result<String> = withContext(Dispatchers.IO) {
        if (config.serverUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请先配置 WebDAV 服务器信息"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            val targetUrl = if (config.serverUrl.endsWith("/")) {
                config.serverUrl + "storage_pro_backup.json"
            } else {
                config.serverUrl + "/storage_pro_backup.json"
            }

            val body = backupJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", credential)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code in 200..204) {
                    val newConfig = config.copy(lastSyncTime = System.currentTimeMillis())
                    saveConfig(context, newConfig)
                    Result.success("云备份上传成功！同步时间已更新")
                } else {
                    Result.failure(Exception("上传失败: HTTP ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("备份上传异常: ${e.localizedMessage}"))
        }
    }

    suspend fun downloadBackup(config: WebDavConfig): Result<String> = withContext(Dispatchers.IO) {
        if (config.serverUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请先配置 WebDAV 服务器信息"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            val targetUrl = if (config.serverUrl.endsWith("/")) {
                config.serverUrl + "storage_pro_backup.json"
            } else {
                config.serverUrl + "/storage_pro_backup.json"
            }

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", credential)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    if (content.isNotBlank()) {
                        Result.success(content)
                    } else {
                        Result.failure(Exception("下载内容为空"))
                    }
                } else {
                    Result.failure(Exception("下载失败: HTTP ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("备份下载异常: ${e.localizedMessage}"))
        }
    }
}

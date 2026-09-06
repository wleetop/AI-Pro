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
        val normalizedUrl = formatServerUrl(config.serverUrl)
        if (normalizedUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请填写完整的 WebDAV 服务器地址与账号"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            
            // 自动确保远端目录存在 (若用户指定了子文件夹)
            ensureDirectoryExists(normalizedUrl, credential)

            val request = Request.Builder()
                .url(normalizedUrl)
                .addHeader("Authorization", credential)
                .method("PROPFIND", "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:allprop/></D:propfind>".toRequestBody("application/xml".toMediaType()))
                .addHeader("Depth", "0")
                .build()

            client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful || response.code == 207 || response.code == 200 || response.code == 204 -> {
                        Result.success("WebDAV 服务连通成功！坚果云/NAS 网盘验证通过")
                    }
                    response.code == 401 -> {
                        Result.failure(Exception("认证失败 (HTTP 401): 请检查账号和应用授权码密码"))
                    }
                    response.code == 404 -> {
                        Result.failure(Exception("路径未找到 (HTTP 404): 请检查 URL 地址。坚果云标准地址: https://dav.jianguoyun.com/dav/StoragePro/"))
                    }
                    else -> {
                        Result.failure(Exception("连接失败: HTTP ${response.code} ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接异常: ${e.localizedMessage}"))
        }
    }

    suspend fun uploadBackup(context: Context, config: WebDavConfig, backupJson: String): Result<String> = withContext(Dispatchers.IO) {
        val normalizedUrl = formatServerUrl(config.serverUrl)
        if (normalizedUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请先配置 WebDAV 服务器信息"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            
            // 确保目录建立
            ensureDirectoryExists(normalizedUrl, credential)

            val targetUrl = getTargetBackupUrl(normalizedUrl)
            val body = backupJson.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", credential)
                .put(body)
                .build()

            var responseCode = -1
            var responseMsg = ""

            client.newCall(request).execute().use { response ->
                responseCode = response.code
                responseMsg = response.message
                if (response.isSuccessful || response.code in 200..207) {
                    val newConfig = config.copy(lastSyncTime = System.currentTimeMillis())
                    saveConfig(context, newConfig)
                    return@withContext Result.success("云备份上传成功！同步时间已更新")
                }
            }

            // 如果第一次遇到 404，尝试创建父路径后再重试一次 PUT
            if (responseCode == 404) {
                ensureDirectoryExists(normalizedUrl, credential)
                client.newCall(request).execute().use { retryResponse ->
                    if (retryResponse.isSuccessful || retryResponse.code in 200..207) {
                        val newConfig = config.copy(lastSyncTime = System.currentTimeMillis())
                        saveConfig(context, newConfig)
                        return@withContext Result.success("云备份上传成功！(已自动建立云端目录)")
                    } else {
                        responseCode = retryResponse.code
                        responseMsg = retryResponse.message
                    }
                }
            }

            if (responseCode == 404) {
                Result.failure(Exception("上传失败 (HTTP 404): 云端目录未找到。若使用坚果云，推荐服务器地址填: https://dav.jianguoyun.com/dav/StoragePro/"))
            } else if (responseCode == 401) {
                Result.failure(Exception("上传失败 (HTTP 401): 账号或授权码不正确"))
            } else {
                Result.failure(Exception("上传失败: HTTP $responseCode $responseMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("备份上传异常: ${e.localizedMessage}"))
        }
    }

    suspend fun downloadBackup(config: WebDavConfig): Result<String> = withContext(Dispatchers.IO) {
        val normalizedUrl = formatServerUrl(config.serverUrl)
        if (normalizedUrl.isBlank() || config.username.isBlank()) {
            return@withContext Result.failure(Exception("请先配置 WebDAV 服务器信息"))
        }

        try {
            val credential = Credentials.basic(config.username, config.password)
            val targetUrl = getTargetBackupUrl(normalizedUrl)

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", credential)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val content = response.body?.string() ?: ""
                        if (content.isNotBlank()) {
                            Result.success(content)
                        } else {
                            Result.failure(Exception("云端备份文件内容为空"))
                        }
                    }
                    response.code == 404 -> {
                        Result.failure(Exception("拉取失败 (HTTP 404): 云端暂无备份文件。请先点击「备份上传」同步当前数据，或检查服务器 URL 路径"))
                    }
                    response.code == 401 -> {
                        Result.failure(Exception("拉取失败 (HTTP 401): 账号或密码/应用授权码不正确"))
                    }
                    else -> {
                        Result.failure(Exception("拉取失败: HTTP ${response.code} ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("备份下载异常: ${e.localizedMessage}"))
        }
    }

    /**
     * 自动规范化 URL，容错坚果云等各种常见的地址写法
     */
    fun formatServerUrl(inputUrl: String): String {
        var url = inputUrl.trim()
        if (url.isBlank()) return ""
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        // 专门矫正坚果云地址:
        if (url.contains("dav.jianguoyun.com")) {
            if (!url.contains("/dav")) {
                url = url.trimEnd('/') + "/dav/"
            }
            // 坚果云根目录 (/dav/) 不允许直接写入裸文件，必须写入子文件夹 (如 /dav/StoragePro/)
            if (url.endsWith("/dav") || url.endsWith("/dav/")) {
                url = url.trimEnd('/') + "/StoragePro/"
            }
        }
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }

    private fun getTargetBackupUrl(normalizedUrl: String): String {
        return normalizedUrl + "storage_pro_backup.json"
    }

    /**
     * 使用 WebDAV MKCOL 智能创建远程目录层级
     */
    private fun ensureDirectoryExists(dirUrl: String, credential: String) {
        try {
            val request = Request.Builder()
                .url(dirUrl)
                .addHeader("Authorization", credential)
                .method("MKCOL", "".toRequestBody(null))
                .build()
            client.newCall(request).execute().close()
        } catch (ignored: Throwable) {}
    }
}

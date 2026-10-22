package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.StorageItem
import com.example.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class UniversalAiVisionService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    companion object {
        private const val PREFS_AI = "storage_pro_ai_prefs"
    }

    fun loadConfig(): ApiConfig {
        val sp = context.getSharedPreferences(PREFS_AI, Context.MODE_PRIVATE)
        val providerStr = sp.getString("provider", ProviderType.HUNYUAN_3.name) ?: ProviderType.HUNYUAN_3.name
        val provider = try {
            ProviderType.valueOf(providerStr)
        } catch (e: Exception) {
            ProviderType.HUNYUAN_3
        }
        val customApiKey = sp.getString("api_key", "") ?: ""
        val effectiveApiKey = customApiKey // Default is blank, prompt user to input their own key
        val useLocalOffline = sp.getBoolean("use_local_offline", false)
        val autoFallback = sp.getBoolean("auto_fallback_local", true)
        val prioritizeLocal = sp.getBoolean("prioritize_local_matching", true)
        val imageMaxDim = sp.getInt("image_max_dim", 640)
        val imageQuality = sp.getInt("image_quality", 70)
        val openAiLowDetail = sp.getBoolean("openai_low_detail", true)
        val fastThinkingMode = sp.getBoolean("fast_thinking_mode", true)

        return ApiConfig(
            provider = provider,
            apiKey = effectiveApiKey,
            hunyuanBaseUrl = sp.getString("hunyuan_base_url", "https://api.hunyuan.cloud.tencent.com/v1") ?: "https://api.hunyuan.cloud.tencent.com/v1",
            hunyuanModel = sp.getString("hunyuan_model", "hunyuan-vision") ?: "hunyuan-vision",
            customBaseUrl = sp.getString("custom_base_url", "https://dashscope.aliyuncs.com/compatible-mode/v1") ?: "https://dashscope.aliyuncs.com/compatible-mode/v1",
            customModel = sp.getString("custom_model", "qwen-vl-plus") ?: "qwen-vl-plus",
            geminiModel = sp.getString("gemini_model", "gemini-3.5-flash") ?: "gemini-3.5-flash",
            useLocalOfflineEngine = useLocalOffline,
            autoFallbackToLocal = autoFallback,
            prioritizeLocalMatching = prioritizeLocal,
            imageMaxDimension = imageMaxDim,
            imageQuality = imageQuality,
            openAiLowDetail = openAiLowDetail,
            fastThinkingMode = fastThinkingMode
        )
    }

    fun saveConfig(config: ApiConfig) {
        val sp = context.getSharedPreferences(PREFS_AI, Context.MODE_PRIVATE)
        sp.edit()
            .putString("provider", config.provider.name)
            .putString("api_key", config.apiKey)
            .putString("hunyuan_base_url", config.hunyuanBaseUrl)
            .putString("hunyuan_model", config.hunyuanModel)
            .putString("custom_base_url", config.customBaseUrl)
            .putString("custom_model", config.customModel)
            .putString("gemini_model", config.geminiModel)
            .putBoolean("use_local_offline", config.useLocalOfflineEngine)
            .putBoolean("auto_fallback_local", config.autoFallbackToLocal)
            .putBoolean("prioritize_local_matching", config.prioritizeLocalMatching)
            .putInt("image_max_dim", config.imageMaxDimension)
            .putInt("image_quality", config.imageQuality)
            .putBoolean("openai_low_detail", config.openAiLowDetail)
            .putBoolean("fast_thinking_mode", config.fastThinkingMode)
            .apply()
    }

    suspend fun testApiConnection(config: ApiConfig = loadConfig()): Result<String> = withContext(Dispatchers.IO) {
        if (config.useLocalOfflineEngine) {
            return@withContext Result.success("本地智能推断引擎处于启用状态 (免 API Key，断网即时响应)")
        }

        val validation = config.validateVisionModel()
        if (!validation.isValid) {
            return@withContext Result.failure(Exception(validation.warning ?: "接口配置有误"))
        }

        if (config.apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API Key 尚未配置，请输入密钥后重试"))
        }
        val startTime = System.currentTimeMillis()
        try {
            val testPrompt = "请简短输出纯JSON：{\"status\":\"ok\"}"
            val res = when (config.provider) {
                ProviderType.HUNYUAN_3 -> callHunyuan3(config, testPrompt, null)
                ProviderType.GEMINI -> callGemini(config, testPrompt, null)
                ProviderType.OPENAI_COMPATIBLE -> callOpenAiCompatible(config, testPrompt, null)
            }
            if (res.isSuccess) {
                val elapsed = System.currentTimeMillis() - startTime
                val providerLabel = when (config.provider) {
                    ProviderType.HUNYUAN_3 -> "腾讯混元 3 (${config.hunyuanModel})"
                    ProviderType.GEMINI -> "Google Gemini 备选 (${config.geminiModel})"
                    ProviderType.OPENAI_COMPATIBLE -> "OpenAI 兼容 (${config.customModel})"
                }
                Result.success("✅ 连接成功！$providerLabel 响应正常，延迟 ${elapsed}ms")
            } else {
                val err = res.exceptionOrNull()?.message ?: "未能收到模型响应"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            val errMsg = when {
                e is java.net.SocketTimeoutException -> "连接超时 (10秒内未连通服务器)。若使用外部接口请检查代理网络，或切换至「本地离线智能引擎」免网极速使用。"
                e is java.net.ConnectException -> "连接被拒绝，目标服务器不可达: ${e.message}"
                e is java.net.UnknownHostException -> "无法解析域名: ${e.message}，请检查 URL 是否填写正确。"
                else -> "测试连通失败: ${e.localizedMessage ?: e.message}"
            }
            Result.failure(Exception(errMsg))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap, config: ApiConfig? = null, overrideMaxDim: Int? = null, overrideQuality: Int? = null): String {
        val maxDim = overrideMaxDim ?: config?.imageMaxDimension ?: 1024
        val quality = overrideQuality ?: config?.imageQuality ?: 85
        val bytes = ImageCompressor.preprocessBitmapForAi(
            source = bitmap,
            maxDimension = maxDim,
            quality = quality,
            useWebP = false // JPEG base64 has universal compatibility across vision providers
        )
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private suspend fun callAi(
        prompt: String,
        bitmap: Bitmap? = null,
        maxTokens: Int = 400,
        overrideMaxDim: Int? = null,
        overrideQuality: Int? = null,
        forceHighDetail: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine) {
            return@withContext Result.failure(Exception("USE_LOCAL_ENGINE"))
        }

        val validation = config.validateVisionModel()
        if (!validation.isValid) {
            return@withContext Result.failure(Exception(validation.warning ?: "模型校验失败"))
        }

        if (config.apiKey.isBlank()) {
            return@withContext Result.failure(Exception("未检测到有效 API Key。请在「设置」中配置密钥，或切换至「本地离线智能引擎」！"))
        }

        try {
            when (config.provider) {
                ProviderType.HUNYUAN_3 -> callHunyuan3(config, prompt, bitmap, maxTokens, overrideMaxDim, overrideQuality, forceHighDetail)
                ProviderType.GEMINI -> callGemini(config, prompt, bitmap, maxTokens, overrideMaxDim, overrideQuality)
                ProviderType.OPENAI_COMPATIBLE -> callOpenAiCompatible(config, prompt, bitmap, maxTokens, overrideMaxDim, overrideQuality, forceHighDetail)
            }
        } catch (e: Exception) {
            val msg = when {
                e is java.net.SocketTimeoutException -> "连接超时: 目标服务器响应超时。建议开启极速模式或使用本地离线引擎。"
                e is java.net.ConnectException -> "网络连接失败: ${e.message}"
                else -> "AI 服务调用异常: ${e.localizedMessage ?: e.message}"
            }
            Result.failure(Exception(msg))
        }
    }

    private fun callHunyuan3(
        config: ApiConfig,
        prompt: String,
        bitmap: Bitmap?,
        maxTokens: Int = 400,
        overrideMaxDim: Int? = null,
        overrideQuality: Int? = null,
        forceHighDetail: Boolean = false
    ): Result<String> {
        val baseUrl = config.getNormalizedBaseUrl()
        val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
        val model = config.hunyuanModel.ifBlank { "hunyuan-vision" }

        val messagesArray = JSONArray()
        val userMessage = JSONObject().put("role", "user")

        if (bitmap != null) {
            val base64 = bitmapToBase64(bitmap, config, overrideMaxDim, overrideQuality)
            val contentArray = JSONArray()
            contentArray.put(JSONObject().put("type", "text").put("text", prompt))
            val imageUrlObj = JSONObject().put("url", "data:image/jpeg;base64,$base64")
            val isLowDetail = if (forceHighDetail) false else config.openAiLowDetail
            if (isLowDetail) {
                imageUrlObj.put("detail", "low")
            } else {
                imageUrlObj.put("detail", "high")
            }
            contentArray.put(
                JSONObject().put("type", "image_url").put("image_url", imageUrlObj)
            )
            userMessage.put("content", contentArray)
        } else {
            userMessage.put("content", prompt)
        }

        messagesArray.put(userMessage)

        val root = JSONObject()
            .put("model", model)
            .put("messages", messagesArray)
            .put("temperature", 0.1)
            .put("max_tokens", maxTokens)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(root.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorDesc = try {
                    val errObj = JSONObject(responseBody).optJSONObject("error")
                    errObj?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                return Result.failure(Exception("腾讯混元 3 接口返回错误 (${response.code}): $errorDesc"))
            }

            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val message = firstChoice?.optJSONObject("message")
            var text = message?.optString("content", "") ?: ""
            if (text.isBlank()) {
                text = message?.optString("reasoning_content", "") ?: ""
            }

            if (text.isBlank()) {
                Result.failure(Exception("未能从腾讯混元 3 获取有效回复内容"))
            } else {
                Result.success(text)
            }
        }
    }

    private fun callGemini(
        config: ApiConfig,
        prompt: String,
        bitmap: Bitmap?,
        maxTokens: Int = 400,
        overrideMaxDim: Int? = null,
        overrideQuality: Int? = null
    ): Result<String> {
        val model = config.geminiModel.ifBlank { "gemini-3.5-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${config.apiKey}"

        fun buildRequestBody(includeThinking: Boolean): JSONObject {
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (bitmap != null) {
                val base64 = bitmapToBase64(bitmap, config, overrideMaxDim, overrideQuality)
                val inlineData = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val contentsArray = JSONArray()
            contentsArray.put(JSONObject().put("parts", partsArray))

            val root = JSONObject().put("contents", contentsArray)

            val generationConfig = JSONObject()
                .put("responseMimeType", "application/json")
                .put("temperature", 0.1)
                .put("maxOutputTokens", maxTokens)

            if (includeThinking && config.fastThinkingMode) {
                val thinkingConfig = JSONObject()
                if (model.startsWith("gemini-3")) {
                    thinkingConfig.put("thinkingLevel", "LOW")
                } else {
                    thinkingConfig.put("thinkingBudget", 0)
                }
                generationConfig.put("thinkingConfig", thinkingConfig)
            }

            root.put("generationConfig", generationConfig)
            return root
        }

        // Try with fast thinking config first, fallback to standard if 400 occurs
        var root = buildRequestBody(includeThinking = config.fastThinkingMode)
        var request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        var response = client.newCall(request).execute()
        if (response.code == 400 && config.fastThinkingMode) {
            response.close()
            // Retry without thinkingConfig in case this specific model variant doesn't accept thinkingConfig
            root = buildRequestBody(includeThinking = false)
            request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
            response = client.newCall(request).execute()
        }

        return response.use { resp ->
            val responseBody = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                val errorDesc = try {
                    val errObj = JSONObject(responseBody).optJSONObject("error")
                    errObj?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                return Result.failure(Exception("Gemini API 返回错误 (${resp.code}): $errorDesc"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            // Collect non-thought text parts (Gemini 2.5/3.5 may return thinking in parts[0])
            val sb = java.lang.StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i) ?: continue
                    val isThought = part.optBoolean("thought", false)
                    val text = part.optString("text", "")
                    if (!isThought && text.isNotBlank()) {
                        sb.append(text)
                    }
                }
            }

            val text = if (sb.isNotEmpty()) {
                sb.toString()
            } else {
                val lastPart = parts?.optJSONObject((parts?.length() ?: 1) - 1)
                lastPart?.optString("text", "") ?: ""
            }

            if (text.isBlank()) {
                Result.failure(Exception("未能从 Gemini 获取有效回复内容"))
            } else {
                Result.success(text)
            }
        }
    }

    private fun callOpenAiCompatible(
        config: ApiConfig,
        prompt: String,
        bitmap: Bitmap?,
        maxTokens: Int = 400,
        overrideMaxDim: Int? = null,
        overrideQuality: Int? = null,
        forceHighDetail: Boolean = false
    ): Result<String> {
        val baseUrl = config.getNormalizedBaseUrl()
        val url = if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"
        val model = config.customModel.ifBlank { "gpt-4o" }

        val messagesArray = JSONArray()
        val userMessage = JSONObject().put("role", "user")

        if (bitmap != null) {
            val base64 = bitmapToBase64(bitmap, config, overrideMaxDim, overrideQuality)
            val contentArray = JSONArray()
            contentArray.put(JSONObject().put("type", "text").put("text", prompt))
            val imageUrlObj = JSONObject().put("url", "data:image/jpeg;base64,$base64")
            val isLowDetail = if (forceHighDetail) false else config.openAiLowDetail
            if (isLowDetail) {
                imageUrlObj.put("detail", "low") // 仅消耗 85 Token，极大降低 Vision 编码排队延时
            } else {
                imageUrlObj.put("detail", "high") // 高清多目标识别与细节定位
            }
            // 通义千问 (Qwen-VL) 与主流视觉模型 1 秒内响应调优:
            // 限制视觉 tokens 数量，通过动态下发 min_pixels 和 max_pixels 避免千问切分过量 patch 造成十几秒的卡顿超时
            val isQwen = model.lowercase().contains("qwen") || baseUrl.lowercase().contains("dashscope")
            if (isQwen && isLowDetail) {
                val maxDim = overrideMaxDim ?: config.imageMaxDimension
                // 512*512 = 262144 像素，对应约 64~100 个 visual tokens，千问推理耗时从 8~15秒 缩短至 300~800ms
                val maxPix = (maxDim * maxDim).coerceAtMost(262144)
                imageUrlObj.put("min_pixels", 3136) // 28 * 28 * 4
                imageUrlObj.put("max_pixels", maxPix)
            } else if (isQwen && !isLowDetail) {
                val maxDim = overrideMaxDim ?: config.imageMaxDimension
                val maxPix = (maxDim * maxDim).coerceAtMost(1024 * 1024)
                imageUrlObj.put("min_pixels", 28 * 28 * 4)
                imageUrlObj.put("max_pixels", maxPix)
            }
            contentArray.put(
                JSONObject().put("type", "image_url").put("image_url", imageUrlObj)
            )
            userMessage.put("content", contentArray)
        } else {
            userMessage.put("content", prompt)
        }

        messagesArray.put(userMessage)

        val root = JSONObject()
            .put("model", model)
            .put("messages", messagesArray)
            .put("temperature", 0.1)
            .put("max_tokens", maxTokens)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(root.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorDesc = try {
                    val errObj = JSONObject(responseBody).optJSONObject("error")
                    errObj?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    responseBody
                }
                return Result.failure(Exception("OpenAI 兼容接口错误 (${response.code}): $errorDesc"))
            }

            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val message = firstChoice?.optJSONObject("message")
            var text = message?.optString("content", "") ?: ""
            if (text.isBlank()) {
                text = message?.optString("reasoning_content", "") ?: ""
            }

            if (text.isBlank()) {
                Result.failure(Exception("未能从模型获取有效回复内容"))
            } else {
                Result.success(text)
            }
        }
    }

    private fun extractJson(rawText: String): String {
        val trimmed = rawText.trim()
        val codeBlockRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val match = codeBlockRegex.find(trimmed)
        val target = match?.groupValues?.get(1)?.trim() ?: trimmed

        val firstBrace = target.indexOf('{')
        val firstBracket = target.indexOf('[')

        val startIdx = when {
            firstBrace != -1 && firstBracket != -1 -> minOf(firstBrace, firstBracket)
            firstBrace != -1 -> firstBrace
            firstBracket != -1 -> firstBracket
            else -> -1
        }

        if (startIdx == -1) return target

        val isArray = target[startIdx] == '['
        val endIdx = if (isArray) target.lastIndexOf(']') else target.lastIndexOf('}')

        if (endIdx != -1 && endIdx >= startIdx) {
            return target.substring(startIdx, endIdx + 1).trim()
        }

        return target
    }

    /**
     * 智能修复大模型返回的非标准或格式缺失 JSON (例如缺失 box 字段左中括号 "[" 等问题)
     */
    private fun repairJsonText(json: String): String {
        var s = json
        // 修复 1: "box": 529, 43, 1056, 456] -> "box": [529, 43, 1056, 456]
        s = s.replace(Regex("(?i)\"box\"\\s*:\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\]")) { match ->
            val (v0, v1, v2, v3) = match.destructured
            "\"box\":[$v0,$v1,$v2,$v3]"
        }
        // 修复 2: "box": [529, 43, 1056, 456 (缺少末尾 ])
        s = s.replace(Regex("(?i)\"box\"\\s*:\\s*\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)(?!\\d|\\])")) { match ->
            val (v0, v1, v2, v3) = match.destructured
            "\"box\":[$v0,$v1,$v2,$v3]"
        }
        // 修复 3: "box": 529, 43, 1056, 456 (完全没有括号，后面跟逗号或右大括号)
        s = s.replace(Regex("(?i)\"box\"\\s*:\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)(?=[,\\}\\s])")) { match ->
            val (v0, v1, v2, v3) = match.destructured
            "\"box\":[$v0,$v1,$v2,$v3]"
        }
        // 修复 4: 移除数组或对象末尾多余的逗号, 如 [1,2,3,] -> [1,2,3]
        s = s.replace(Regex(",\\s*([\\]\\}])"), "$1")
        return s
    }

    // 1. 单件物品拍照识别
    suspend fun recognizeSingleItem(bitmap: Bitmap): Result<SingleItemRecognitionResult> = withContext(Dispatchers.Default) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(LocalHeuristicEngine.recognizeSingleItem(bitmap))
        }

        val prompt = """
            分析图内主要物品，仅输出紧凑JSON:
            {
              "name": "具体品名",
              "category": "电子数码|日常居家|服饰箱包|食品饮料|药品个护|户外露营|重要证件|美妆护肤|工具五金|文具书籍",
              "suggestedLocation": "推荐收纳位置",
              "quantity": 1,
              "unit": "件",
              "estimatedPrice": 估计价格数值或null,
              "expiryDays": 保质天数或null,
              "tags": ["标签1", "标签2"],
              "storageTips": "20字以内保养建议"
            }
        """.trimIndent()

        val rawResult = callAi(prompt, bitmap, maxTokens = 350)
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                val local = LocalHeuristicEngine.recognizeSingleItem(bitmap)
                return@withContext Result.success(
                    local.copy(
                        storageTips = "${local.storageTips} (云端接口无法连通，已无缝切换本地离线引擎秒级完成解析)"
                    )
                )
            }
            return@withContext Result.failure(ex ?: Exception("识别请求失败"))
        }

        try {
            val jsonText = repairJsonText(extractJson(rawResult.getOrThrow()))
            val obj = JSONObject(jsonText)
            val tagsArray = obj.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(i))
                }
            }

            Result.success(
                SingleItemRecognitionResult(
                    name = obj.optString("name", "未命名物品"),
                    category = obj.optString("category", "日常居家"),
                    suggestedLocation = obj.optString("suggestedLocation", "客厅/收纳柜"),
                    quantity = obj.optInt("quantity", 1),
                    unit = obj.optString("unit", "件"),
                    estimatedPrice = if (obj.has("estimatedPrice") && !obj.isNull("estimatedPrice")) obj.optDouble("estimatedPrice") else null,
                    expiryDays = if (obj.has("expiryDays") && !obj.isNull("expiryDays")) obj.optInt("expiryDays") else null,
                    tags = tags,
                    storageTips = obj.optString("storageTips", "妥善分类存放于干燥通风处")
                )
            )
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                val local = LocalHeuristicEngine.recognizeSingleItem(bitmap)
                Result.success(
                    local.copy(
                        storageTips = "${local.storageTips} (云端解析异常，已由本地离线引擎接管)"
                    )
                )
            } else {
                Result.failure(Exception("解析识别结果失败: ${e.message}"))
            }
        }
    }

    // 2. 仅输入名称进行语义联想与收纳建议（可配置是否优先尝试本地基础权重与历史语义匹配）
    suspend fun suggestForNameOnly(itemName: String, historyItems: List<StorageItem> = emptyList()): Result<SingleItemRecognitionResult> = withContext(Dispatchers.Default) {
        val localMatch = LocalHeuristicEngine.matchCategoryAndInfoLocally(itemName, historyItems)
        val config = loadConfig()

        // 1) 处于纯本地离线模式 / 未填 API Key 时：必定直接返回本地匹配结果
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(localMatch.result)
        }

        // 2) 开启了“发起云端 API 请求前，优先执行本地特征匹配”选项目标，且本地达到了高置信度命中（历史精确/模糊命中或高权重分类）：直接返回本地结果，免发网络请求
        if (config.prioritizeLocalMatching && localMatch.isMatched) {
            return@withContext Result.success(localMatch.result)
        }

        val prompt = """
            物品「$itemName」，推测收纳属性并仅输出紧凑JSON:
            {
              "name": "$itemName",
              "category": "电子数码|日常居家|服饰箱包|食品饮料|药品个护|户外露营|重要证件|美妆护肤|工具五金|文具书籍",
              "suggestedLocation": "推荐收纳位置",
              "quantity": 1,
              "unit": "件",
              "expiryDays": 保质天数或null,
              "tags": ["标签1", "标签2"],
              "storageTips": "20字以内保养建议"
            }
        """.trimIndent()

        val rawResult = callAi(prompt, null, maxTokens = 250)
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                return@withContext Result.success(localMatch.result)
            }
            return@withContext Result.failure(ex ?: Exception("建议生成失败"))
        }

        try {
            val jsonText = repairJsonText(extractJson(rawResult.getOrThrow()))
            val obj = JSONObject(jsonText)
            val tagsArray = obj.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(i))
                }
            }

            Result.success(
                SingleItemRecognitionResult(
                    name = obj.optString("name", itemName),
                    category = obj.optString("category", "日常居家"),
                    suggestedLocation = obj.optString("suggestedLocation", "未分类位置"),
                    quantity = obj.optInt("quantity", 1),
                    unit = obj.optString("unit", "件"),
                    expiryDays = if (obj.has("expiryDays") && !obj.isNull("expiryDays")) obj.optInt("expiryDays") else null,
                    tags = tags,
                    storageTips = obj.optString("storageTips", "")
                )
            )
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                Result.success(localMatch.result)
            } else {
                Result.failure(Exception("解析建议失败: ${e.message}"))
            }
        }
    }

    // 3. ListPlus 批量识图入库 (物品“全家福”多目标拆解)
    suspend fun recognizeBatchListPlus(bitmap: Bitmap): Result<List<BatchRecognizedItem>> = withContext(Dispatchers.Default) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(LocalHeuristicEngine.recognizeBatchListPlus(bitmap))
        }

        val prompt = """
            请识别并拆解画面中所有可见的独立物品，仅输出紧凑JSON数组(不要任何多余说明或markdown):
            [{"name":"品名","category":"日常居家","suggestedLocation":"推荐位置","quantity":1,"unit":"件","tags":["特征"],"box":[ymin,xmin,ymax,xmax]}]
            精确定位要求:
            1. box必须是带有中括号[]的4元整数数组 [ymin, xmin, ymax, xmax]，取值 0-1000 整数。绝对禁止遗漏中括号 []！
            2. box 必须紧密包裹该物品的真实物理外观边界，绝对不能包含周边大面积的桌台、墙面或地面背景！
            3. 不要把背景家具（如台面、桌面、净化器顶）识别为独立收纳物品，仅识别放置在其上的独立物品。
            分类选其一:电子数码|日常居家|服饰箱包|食品饮料|药品个护|户外露营|重要证件|美妆护肤|工具五金|文具书籍
        """.trimIndent()

        // 批量多物品全家福图像对分辨率敏感，确保使用至少 1024px 高清长边及高细节模式
        val batchMaxDim = maxOf(config.imageMaxDimension, 1024)
        val batchQuality = maxOf(config.imageQuality, 85)
        val rawResult = callAi(
            prompt = prompt,
            bitmap = bitmap,
            maxTokens = 1500,
            overrideMaxDim = batchMaxDim,
            overrideQuality = batchQuality,
            forceHighDetail = true
        )
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                return@withContext Result.success(LocalHeuristicEngine.recognizeBatchListPlus(bitmap))
            }
            return@withContext Result.failure(ex ?: Exception("批量识图失败"))
        }

        try {
            val rawString = rawResult.getOrThrow()
            val extractedJson = extractJson(rawString)
            val cleanJsonText = repairJsonText(extractedJson)
            val list = mutableListOf<BatchRecognizedItem>()

            // 优先尝试标准的 JSONArray 解析
            val array = try {
                JSONArray(cleanJsonText)
            } catch (e: Exception) {
                // 如果标准 JSONArray 仍然由于局部格式损坏解析失败，使用正则表达式提取单个 JSONObject 兜底
                val objectMatches = Regex("\\{[^{}]*\"name\"[^{}]*\\}").findAll(rawString)
                val fallbackArray = JSONArray()
                for (m in objectMatches) {
                    try {
                        val repairedObjStr = repairJsonText(m.value)
                        fallbackArray.put(JSONObject(repairedObjStr))
                    } catch (_: Exception) {}
                }
                if (fallbackArray.length() > 0) {
                    fallbackArray
                } else {
                    throw e
                }
            }

            for (i in 0 until array.length()) {
                try {
                    val obj = array.getJSONObject(i)
                    val tagsArray = obj.optJSONArray("tags")
                    val tags = mutableListOf<String>()
                    if (tagsArray != null) {
                        for (t in 0 until tagsArray.length()) {
                            tags.add(tagsArray.getString(t))
                        }
                    }
                    val boxObj = obj.opt("box")
                    val boxNorm = parseBoxCoordinates(boxObj)

                    list.add(
                        BatchRecognizedItem(
                            id = UUID.randomUUID().toString(),
                            name = obj.optString("name", "物品 ${i + 1}"),
                            category = obj.optString("category", "日常居家"),
                            suggestedLocation = obj.optString("suggestedLocation", "默认收纳箱"),
                            quantity = obj.optInt("quantity", 1),
                            unit = obj.optString("unit", "件"),
                            tags = tags,
                            isSelected = true,
                            boxNorm = boxNorm
                        )
                    )
                } catch (_: Exception) {}
            }

            if (list.isNotEmpty()) {
                Result.success(list)
            } else {
                if (config.autoFallbackToLocal) {
                    Result.success(LocalHeuristicEngine.recognizeBatchListPlus(bitmap))
                } else {
                    Result.failure(Exception("未能从云端回应中提取出有效物品"))
                }
            }
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                Result.success(LocalHeuristicEngine.recognizeBatchListPlus(bitmap))
            } else {
                Result.failure(Exception("解析批量物品清单失败: ${e.message}"))
            }
        }
    }

    // 4. 购物小票 / 快递单解析
    suspend fun parseReceiptOrLabel(bitmap: Bitmap): Result<ReceiptParseResult> = withContext(Dispatchers.Default) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(LocalHeuristicEngine.parseReceipt())
        }

        val prompt = """
            提取购物小票或快递面单信息，仅输出紧凑JSON:
            {
              "merchantName": "商家/平台名",
              "purchaseDate": "日期(YYYY-MM-DD)",
              "totalAmount": 总金额数值或null,
              "items": [
                {
                  "name": "品名",
                  "category": "食品饮料|日常居家|药品个护|电子数码|美妆护肤",
                  "price": 单价数值或null,
                  "quantity": 1,
                  "unit": "件"
                }
              ]
            }
        """.trimIndent()

        val rawResult = callAi(prompt, bitmap, maxTokens = 700)
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                return@withContext Result.success(LocalHeuristicEngine.parseReceipt())
            }
            return@withContext Result.failure(ex ?: Exception("小票解析失败"))
        }

        try {
            val jsonText = extractJson(rawResult.getOrThrow())
            val obj = JSONObject(jsonText)
            val itemsArray = obj.optJSONArray("items")
            val items = mutableListOf<ReceiptParsedItem>()
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itObj = itemsArray.getJSONObject(i)
                    items.add(
                        ReceiptParsedItem(
                            name = itObj.optString("name", "小票商品"),
                            category = itObj.optString("category", "食品饮料"),
                            price = if (itObj.has("price") && !itObj.isNull("price")) itObj.optDouble("price") else null,
                            quantity = itObj.optInt("quantity", 1),
                            unit = itObj.optString("unit", "件"),
                            isSelected = true
                        )
                    )
                }
            }

            Result.success(
                ReceiptParseResult(
                    merchantName = obj.optString("merchantName", "购物小票"),
                    purchaseDate = obj.optString("purchaseDate", ""),
                    totalAmount = if (obj.has("totalAmount") && !obj.isNull("totalAmount")) obj.optDouble("totalAmount") else null,
                    items = items
                )
            )
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                Result.success(LocalHeuristicEngine.parseReceipt())
            } else {
                Result.failure(Exception("解析小票数据失败: ${e.message}"))
            }
        }
    }

    // 5. 语义化模糊搜索 (如: "去海边玩需要带什么？")
    suspend fun semanticSearch(query: String, inventory: List<StorageItem>): Result<SemanticSearchResult> = withContext(Dispatchers.Default) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(LocalHeuristicEngine.semanticSearch(query, inventory))
        }

        if (inventory.isEmpty()) {
            return@withContext Result.success(SemanticSearchResult(emptyList(), "当前物品库为空"))
        }

        // 紧凑文本编码，节省 80%+ 的 Prompt Tokens
        val compactCatalog = inventory.take(100).joinToString("\n") {
            val tagStr = if (it.tags.isNotBlank()) "#${it.tags}" else ""
            "${it.id}:${it.name}|${it.category}|${it.locationPath}|$tagStr"
        }

        val prompt = """
            用户意图:「$query」。在下方物品清单中挑选最匹配的ID列表，仅输出紧凑JSON:
            {"matchedItemIds": [1, 2], "explanation": "一句话推荐说明"}
            清单:
            $compactCatalog
        """.trimIndent()

        val rawResult = callAi(prompt, null, maxTokens = 250)
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                return@withContext Result.success(LocalHeuristicEngine.semanticSearch(query, inventory))
            }
            return@withContext Result.failure(ex ?: Exception("语义搜索失败"))
        }

        try {
            val jsonText = repairJsonText(extractJson(rawResult.getOrThrow()))
            val obj = JSONObject(jsonText)
            val idsArray = obj.optJSONArray("matchedItemIds")
            val ids = mutableListOf<Long>()
            if (idsArray != null) {
                for (i in 0 until idsArray.length()) {
                    ids.add(idsArray.getLong(i))
                }
            }
            Result.success(
                SemanticSearchResult(
                    matchedItemIds = ids,
                    explanation = obj.optString("explanation", "AI 语义检索匹配完成")
                )
            )
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                Result.success(LocalHeuristicEngine.semanticSearch(query, inventory))
            } else {
                Result.failure(Exception("语义搜索解析失败: ${e.message}"))
            }
        }
    }

    // 6. 断舍离建议生成 (根据物品闲置/更新时间/临期状态生成丢弃/闲鱼转让建议)
    suspend fun generateDeclutterReport(inventory: List<StorageItem>): Result<List<DeclutterAdvice>> = withContext(Dispatchers.Default) {
        val config = loadConfig()
        if (config.useLocalOfflineEngine || config.apiKey.isBlank()) {
            return@withContext Result.success(LocalHeuristicEngine.generateDeclutterReport(inventory))
        }

        if (inventory.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        val now = System.currentTimeMillis()
        val compactCatalog = inventory.take(60).joinToString("\n") {
            val idleDays = ((now - it.updatedAt) / (24 * 3600 * 1000)).toInt()
            val exp = if (it.expiryDate != null && it.expiryDate < now) "已过期" else ""
            "${it.id}:${it.name}|${it.category}|${it.locationPath}|闲置${idleDays}天|$exp"
        }

        val prompt = """
            根据以下物品闲置与临期数据，挑选3~8件最值得断舍离的物品，仅输出JSON数组:
            [{"itemId": 1, "itemName": "品名", "category": "分类", "location": "位置", "reason": "闲置超300天", "actionType": "闲鱼转让|直接丢弃|捐赠环保|重新归类"}]
            数据:
            $compactCatalog
        """.trimIndent()

        val rawResult = callAi(prompt, null, maxTokens = 650)
        if (rawResult.isFailure) {
            val ex = rawResult.exceptionOrNull()
            if (config.autoFallbackToLocal || ex?.message == "USE_LOCAL_ENGINE") {
                return@withContext Result.success(LocalHeuristicEngine.generateDeclutterReport(inventory))
            }
            return@withContext Result.failure(ex ?: Exception("断舍离分析失败"))
        }

        try {
            val jsonText = repairJsonText(extractJson(rawResult.getOrThrow()))
            val array = JSONArray(jsonText)
            val adviceList = mutableListOf<DeclutterAdvice>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                adviceList.add(
                    DeclutterAdvice(
                        itemId = obj.optLong("itemId", 0),
                        itemName = obj.optString("itemName", ""),
                        category = obj.optString("category", ""),
                        location = obj.optString("location", ""),
                        reason = obj.optString("reason", "建议定期清理闲置"),
                        actionType = obj.optString("actionType", "闲鱼转让")
                    )
                )
            }
            Result.success(adviceList)
        } catch (e: Exception) {
            if (config.autoFallbackToLocal) {
                Result.success(LocalHeuristicEngine.generateDeclutterReport(inventory))
            } else {
                Result.failure(Exception("断舍离建议解析失败: ${e.message}"))
            }
        }
    }

    private fun parseBoxCoordinates(boxObj: Any?): List<Int>? {
        if (boxObj == null) return null
        val numbers = mutableListOf<Double>()

        try {
            if (boxObj is JSONArray) {
                if (boxObj.length() != 4) return null
                for (i in 0 until 4) {
                    numbers.add(boxObj.getDouble(i))
                }
            } else {
                val str = boxObj.toString()
                val matches = Regex("-?\\d+(?:\\.\\d+)?").findAll(str).mapNotNull { it.value.toDoubleOrNull() }.toList()
                if (matches.size == 4) {
                    numbers.addAll(matches)
                } else {
                    return null
                }
            }

            if (numbers.size != 4 || numbers.any { it.isNaN() }) return null

            val v0 = numbers[0]
            val v1 = numbers[1]
            val v2 = numbers[2]
            val v3 = numbers[3]

            // 判别是否为 0.0 ~ 1.0 的浮点归一化数值 (OpenAI/QwenVL 常见)
            val isFloatNorm = v0 <= 1.0 && v1 <= 1.0 && v2 <= 1.0 && v3 <= 1.0 && (v0 > 0 || v1 > 0 || v2 > 0 || v3 > 0)
            val mult = if (isFloatNorm) 1000.0 else 1.0

            var ymin = (v0 * mult).toInt()
            var xmin = (v1 * mult).toInt()
            var ymax = (v2 * mult).toInt()
            var xmax = (v3 * mult).toInt()

            // 防翻转处理
            if (ymin > ymax) { val tmp = ymin; ymin = ymax; ymax = tmp }
            if (xmin > xmax) { val tmp = xmin; xmin = xmax; xmax = tmp }

            // 限制范围在 0..1000
            ymin = ymin.coerceIn(0, 1000)
            xmin = xmin.coerceIn(0, 1000)
            ymax = ymax.coerceIn(ymin + 1, 1000)
            xmax = xmax.coerceIn(xmin + 1, 1000)

            // 过滤极微小碎片与超大背景框 (小于 0.1% 或 大于 95% 视为非有效具体物品框)
            val w = xmax - xmin
            val h = ymax - ymin
            val areaRatio = (w.toDouble() * h.toDouble()) / 1000000.0
            if (areaRatio < 0.001 || areaRatio > 0.95) {
                return null
            }

            return listOf(ymin, xmin, ymax, xmax)
        } catch (e: Exception) {
            return null
        }
    }
}


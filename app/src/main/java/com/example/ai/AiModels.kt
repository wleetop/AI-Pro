package com.example.ai

data class ApiConfig(
    val provider: ProviderType = ProviderType.GEMINI,
    val apiKey: String = "",
    val customBaseUrl: String = "https://api.openai.com/v1",
    val customModel: String = "gpt-4o",
    val geminiModel: String = "gemini-3.5-flash",
    val useLocalOfflineEngine: Boolean = false,
    val autoFallbackToLocal: Boolean = true,
    // 传输与Token性能优化 (针对全家福多物批量识别与单物识别自动优化)
    val imageMaxDimension: Int = 1024,    // 默认提升至 1024px，确保多物品细节清晰，可选 640/1024/1280
    val imageQuality: Int = 85,           // JPEG压缩质量：提升至 85% 避免边缘锯齿导致误识别
    val openAiLowDetail: Boolean = false, // 批量全家福识别默认采用高细节模式(high detail)，确保小物体可辨
    val fastThinkingMode: Boolean = true  // Gemini 极速思考模式 (跳过漫长思考直接输出)
) {
    fun getEffectiveModel(): String {
        return if (useLocalOfflineEngine) "本地离线智能引擎" else if (provider == ProviderType.GEMINI) geminiModel else customModel
    }

    fun getNormalizedBaseUrl(): String {
        val trimmed = customBaseUrl.trim()
        if (trimmed.isBlank()) return "https://api.openai.com/v1"
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed.trimEnd('/')
        } else {
            "https://$trimmed".trimEnd('/')
        }
    }

    /**
     * 防呆拦截：检查模型名称是否为画图/纯语音/视频/嵌入等非视觉大模型，以及 URL 是否配置错误
     */
    fun validateVisionModel(): ModelValidationResult {
        if (useLocalOfflineEngine) {
            return ModelValidationResult(isValid = true, warning = null)
        }

        // 检查 OpenAI 兼容模式下的 Base URL 是否配置为网页分享链接（如 share.gemini.google）
        if (provider == ProviderType.OPENAI_COMPATIBLE) {
            val urlLower = customBaseUrl.lowercase().trim()
            if (urlLower.contains("share.gemini.google") || urlLower.contains("gemini.google.com")) {
                return ModelValidationResult(
                    isValid = false,
                    warning = "配置错误拦截：您填写的「$customBaseUrl」是 Google Gemini 网页分享链接，不是标准 API 接口地址！若需使用 Gemini，请将上方接口类型选为「Google Gemini 官方」；若使用通义千问/智谱等，请填写相应 API Base URL。"
                )
            }
        }

        val model = getEffectiveModel().lowercase().trim()
        val prohibitedPatterns = listOf(
            "dall-e", "midjourney", "stable-diffusion", "flux",
            "sora", "runway", "pika", "kling",
            "tts", "whisper", "speech", "audio",
            "embedding", "rerank", "moderation"
        )
        for (pattern in prohibitedPatterns) {
            if (model.contains(pattern)) {
                return ModelValidationResult(
                    isValid = false,
                    warning = "拦截错误模型「$model」：该模型为画图/语音/视频/嵌入模型，无法进行视觉识别与收纳分析，请更换为视觉模型（如 gemini-3.5-flash, gpt-4o, qwen-vl-max, glm-4v）"
                )
            }
        }
        return ModelValidationResult(isValid = true, warning = null)
    }
}

enum class ProviderType {
    GEMINI,
    OPENAI_COMPATIBLE
}

data class ModelValidationResult(
    val isValid: Boolean,
    val warning: String?
)

data class SingleItemRecognitionResult(
    val name: String,
    val category: String,
    val suggestedLocation: String,
    val quantity: Int = 1,
    val unit: String = "件",
    val estimatedPrice: Double? = null,
    val expiryDays: Int? = null,
    val tags: List<String> = emptyList(),
    val storageTips: String = ""
)

data class BatchRecognizedItem(
    val id: String,
    val name: String,
    val category: String,
    val suggestedLocation: String,
    val quantity: Int = 1,
    val unit: String = "件",
    val tags: List<String> = emptyList(),
    var isSelected: Boolean = true,
    var imagePath: String? = null,
    val boxNorm: List<Int>? = null, // [ymin, xmin, ymax, xmax] in 0..1000 scale
    var selectedSpace: String = suggestedLocation,
    var containerId: Long? = null,
    var containerName: String? = null
)

data class ReceiptParseResult(
    val merchantName: String = "",
    val purchaseDate: String = "",
    val totalAmount: Double? = null,
    val items: List<ReceiptParsedItem> = emptyList()
)

data class ReceiptParsedItem(
    val name: String,
    val category: String,
    val price: Double?,
    val quantity: Int = 1,
    val unit: String = "件",
    var isSelected: Boolean = true
)

data class DeclutterAdvice(
    val itemId: Long,
    val itemName: String,
    val category: String,
    val location: String,
    val reason: String,
    val actionType: String // "闲鱼转让", "直接丢弃", "捐赠环保", "重新归类"
)

data class SemanticSearchResult(
    val matchedItemIds: List<Long>,
    val explanation: String
)


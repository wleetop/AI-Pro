package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ApiConfig
import com.example.ai.BatchRecognizedItem
import com.example.ai.DeclutterAdvice
import com.example.ai.ReceiptParseResult
import com.example.ai.SemanticSearchResult
import com.example.ai.SingleItemRecognitionResult
import com.example.ai.UniversalAiVisionService
import com.example.data.AiRecognitionCache
import com.example.data.LocationNode
import com.example.data.StorageCategory
import com.example.data.StorageContainer
import com.example.data.StorageDatabase
import com.example.data.StorageItem
import com.example.repository.StorageRepository
import com.example.util.BackupManager
import com.example.util.ImageCompressor
import com.example.util.QrCodeUtils
import com.example.util.SecurityLockManager
import com.example.util.SecuritySettings
import com.example.util.WebDavConfig
import com.example.util.WebDavManager
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.example.ai.LocalHeuristicEngine
import com.example.ui.theme.AppThemePreset

enum class ItemSortOrder(val label: String) {
    UPDATED_DESC("最近更新"),
    EXPIRY_ASC("到期优先"),
    NAME_ASC("名称拼音"),
    PRICE_DESC("价值由高到低")
}

sealed class AiOperationState {
    object Idle : AiOperationState()
    data class Loading(val message: String) : AiOperationState()
    data class SingleSuccess(val result: SingleItemRecognitionResult, val compressedImagePath: String? = null) : AiOperationState()
    data class BatchStreaming(val items: List<BatchRecognizedItem>, val isComplete: Boolean = false, val statusText: String = "") : AiOperationState()
    data class BatchSuccess(val items: List<BatchRecognizedItem>) : AiOperationState()
    data class ReceiptSuccess(val result: ReceiptParseResult) : AiOperationState()
    data class DeclutterSuccess(val adviceList: List<DeclutterAdvice>) : AiOperationState()
    data class SemanticSuccess(val result: SemanticSearchResult) : AiOperationState()
    data class Error(val errorMsg: String) : AiOperationState()
}

data class RestoreProgressState(
    val isRestoring: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val stage: String = ""
)

data class FilterParams(
    val query: String,
    val category: String?,
    val containerId: Long?,
    val sortOrder: ItemSortOrder
)

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StorageRepository(StorageDatabase.getInstance(application))
    private val aiService = UniversalAiVisionService(application)

    // Data streams
    val allItems: StateFlow<List<StorageItem>> = repository.allActiveItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContainers: StateFlow<List<StorageContainer>> = repository.allContainers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<StorageCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocations: StateFlow<List<LocationNode>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter & Search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedContainerFilter = MutableStateFlow<Long?>(null)
    val selectedContainerFilter = _selectedContainerFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(ItemSortOrder.UPDATED_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    private val _onlyExpiringSoon = MutableStateFlow(false)
    val onlyExpiringSoon = _onlyExpiringSoon.asStateFlow()

    // Semantic matched item IDs if semantic search was run
    private val _semanticMatchedIds = MutableStateFlow<List<Long>?>(null)
    val semanticMatchedIds = _semanticMatchedIds.asStateFlow()

    private val filterCriteria = combine(
        _searchQuery,
        _selectedCategory,
        _selectedContainerFilter,
        _sortOrder
    ) { query, cat, containerId, sort ->
        FilterParams(query, cat, containerId, sort)
    }

    private val secondaryCriteria = combine(
        _onlyExpiringSoon,
        _semanticMatchedIds
    ) { onlyExpiring, semIds ->
        Pair(onlyExpiring, semIds)
    }

    // Combined filtered items
    val filteredItems: StateFlow<List<StorageItem>> = combine(
        allItems,
        filterCriteria,
        secondaryCriteria
    ) { items, filters, secondary ->
        val query = filters.query
        val cat = filters.category
        val containerId = filters.containerId
        val sort = filters.sortOrder
        val onlyExpiring = secondary.first
        val semIds = secondary.second

        val now = System.currentTimeMillis()
        val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

        var list = items

        // If semantic search active
        if (semIds != null) {
            list = list.filter { semIds.contains(it.id) }
        }

        // Search text
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.locationPath.lowercase().contains(q) ||
                        (it.notes?.lowercase()?.contains(q) == true) ||
                        it.tags.lowercase().contains(q) ||
                        (it.barcode?.lowercase()?.contains(q) == true)
            }
        }

        // Category filter
        if (cat != null) {
            list = list.filter { it.category == cat }
        }

        // Container filter
        if (containerId != null) {
            list = list.filter { it.containerId == containerId }
        }

        // Only expiring soon (< 30 days)
        if (onlyExpiring) {
            list = list.filter { it.expiryDate != null && it.expiryDate <= (now + thirtyDaysMillis) }
        }

        // Sorting
        when (sort) {
            ItemSortOrder.UPDATED_DESC -> list.sortedByDescending { it.updatedAt }
            ItemSortOrder.EXPIRY_ASC -> list.sortedWith(
                compareBy(
                    { it.expiryDate == null },
                    { it.expiryDate }
                )
            )
            ItemSortOrder.NAME_ASC -> list.sortedBy { it.name }
            ItemSortOrder.PRICE_DESC -> list.sortedByDescending { it.price ?: 0.0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Network status
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    // AI Operation state
    private val _aiState = MutableStateFlow<AiOperationState>(AiOperationState.Idle)
    val aiState = _aiState.asStateFlow()

    // Global Toast / Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    // Security Lock state
    private val _securitySettings = MutableStateFlow(SecurityLockManager.loadSettings(application))
    val securitySettings = _securitySettings.asStateFlow()

    private val _isAppLocked = MutableStateFlow(_securitySettings.value.isLockEnabled && _securitySettings.value.pinCode.isNotBlank())
    val isAppLocked = _isAppLocked.asStateFlow()

    // WebDAV config state
    private val _webDavConfig = MutableStateFlow(WebDavManager.loadConfig(application))
    val webDavConfig = _webDavConfig.asStateFlow()

    // Backup Restore state
    private val _restoreProgress = MutableStateFlow(RestoreProgressState())
    val restoreProgress = _restoreProgress.asStateFlow()

    // AI config
    private val _apiConfig = MutableStateFlow(aiService.loadConfig())
    val apiConfig = _apiConfig.asStateFlow()

    // App Theme Preset state
    private val themePrefs = application.getSharedPreferences("storage_pro_theme_prefs", Context.MODE_PRIVATE)
    private val _currentThemePreset = MutableStateFlow(
        run {
            val savedThemeId = themePrefs.getString("theme_preset", AppThemePreset.ROYAL_PURPLE.id)
            AppThemePreset.values().firstOrNull { it.id == savedThemeId } ?: AppThemePreset.ROYAL_PURPLE
        }
    )
    val currentThemePreset = _currentThemePreset.asStateFlow()

    fun setThemePreset(preset: AppThemePreset) {
        _currentThemePreset.value = preset
        themePrefs.edit().putString("theme_preset", preset.id).apply()
    }

    init {
        monitorNetwork()
    }

    private fun monitorNetwork() {
        try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val initialNetwork = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(initialNetwork)
            _isOnline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            })
        } catch (e: Exception) {
            // Gracefully fallback to online if network manager callback fails on emulator
            _isOnline.value = true
        }
    }

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _semanticMatchedIds.value = null
        }
    }

    fun setSelectedCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    fun setSelectedContainerFilter(containerId: Long?) {
        _selectedContainerFilter.value = containerId
    }

    fun setSortOrder(order: ItemSortOrder) {
        _sortOrder.value = order
    }

    fun toggleOnlyExpiringSoon() {
        _onlyExpiringSoon.value = !_onlyExpiringSoon.value
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedContainerFilter.value = null
        _onlyExpiringSoon.value = false
        _semanticMatchedIds.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun resetAiState() {
        _aiState.value = AiOperationState.Idle
    }

    // CRUD for Items
    fun saveItem(item: StorageItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
                showMessage("物品已妥善收纳入库: ${item.name}")
            } else {
                repository.updateItem(item)
                showMessage("物品信息已更新: ${item.name}")
            }
        }
    }

    fun deleteItem(item: StorageItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            showMessage("已删除物品: ${item.name}")
        }
    }

    // Batch operations
    fun batchDeleteItems(itemIds: Set<Long>) {
        if (itemIds.isEmpty()) return
        viewModelScope.launch {
            repository.deleteItemsByIds(itemIds.toList())
            showMessage("已批量删除 ${itemIds.size} 件物品")
        }
    }

    fun batchMoveItemsToContainer(itemIds: Set<Long>, container: StorageContainer?) {
        if (itemIds.isEmpty()) return
        viewModelScope.launch {
            val location = container?.locationPath ?: "未分配容器"
            repository.batchMoveItems(itemIds.toList(), container?.id, location)
            showMessage("已将 ${itemIds.size} 件物品移入「${container?.name ?: "无容器"}」")
        }
    }

    fun batchUpdateCategory(itemIds: Set<Long>, category: String) {
        if (itemIds.isEmpty()) return
        viewModelScope.launch {
            repository.batchUpdateCategory(itemIds.toList(), category)
            showMessage("已批量将 ${itemIds.size} 件物品修改分类为「$category」")
        }
    }

    // Batch insert items (e.g. from ListPlus or Receipt)
    fun batchSaveItems(items: List<StorageItem>) {
        viewModelScope.launch {
            repository.insertItems(items)
            showMessage("已批量存入 ${items.size} 件物品到本地库")
        }
    }

    // Containers CRUD
    fun saveContainer(container: StorageContainer) {
        viewModelScope.launch {
            if (container.id == 0L) {
                repository.insertContainer(container)
                showMessage("已创建收纳空间/箱: ${container.name}")
            } else {
                repository.updateContainer(container)
                showMessage("空间信息已更新: ${container.name}")
            }
        }
    }

    fun deleteContainer(container: StorageContainer) {
        viewModelScope.launch {
            repository.deleteContainer(container)
            showMessage("已删除容器: ${container.name}")
        }
    }

    // Category CRUD
    fun addCustomCategory(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            val cat = StorageCategory(name = name, icon = icon, colorHex = colorHex, isCustom = true)
            repository.insertCategory(cat)
            showMessage("新增自定义分类: $name")
        }
    }

    fun deleteCategory(category: StorageCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showMessage("已移除分类: ${category.name}")
        }
    }

    // Image compression helper
    fun compressAndSaveImageUri(uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val path = ImageCompressor.compressAndSaveImage(getApplication(), uri)
            onResult(path)
        }
    }

    fun compressAndSaveBitmap(bitmap: Bitmap, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val path = ImageCompressor.compressBitmapAndSave(getApplication(), bitmap)
            onResult(path)
        }
    }

    // AI Operations
    private var lastSingleBitmap: Bitmap? = null
    var lastBatchBitmap: Bitmap? = null
        private set

    fun recognizeSinglePhoto(bitmap: Bitmap) {
        lastSingleBitmap = bitmap
        viewModelScope.launch {
            try {
                // Step 1: Pre-process with Canvas compression and save thumbnail safely
                val localPath = try {
                    ImageCompressor.compressBitmapAndSave(getApplication(), bitmap)
                } catch (e: Throwable) {
                    null
                }

                // Check if API key is blank and user is not in offline engine mode
                val currentConfig = _apiConfig.value
                if (currentConfig.apiKey.isBlank() && !currentConfig.useLocalOfflineEngine) {
                    showMessage("未设置云服务，请设置，当前已自动转为本地识图")
                    val local = LocalHeuristicEngine.recognizeSingleItem(bitmap)
                    val finalResult = local.copy(
                        storageTips = "${local.storageTips} (未设置云端密钥，已自动转为本地识图)"
                    )
                    _aiState.value = AiOperationState.SingleSuccess(finalResult, localPath)
                    return@launch
                }

                val compressedBytes = try {
                    ImageCompressor.preprocessBitmapForAi(bitmap, maxDimension = 640, quality = 75)
                } catch (e: Throwable) {
                    null
                }

                // Step 2: Check high-frequency cache for instant 0ms result
                val hashKey = if (compressedBytes != null) "IMG_${bitmap.width}x${bitmap.height}_${compressedBytes.size.hashCode()}" else null
                val cached = if (hashKey != null) {
                    try { repository.getAiCache(hashKey) } catch (e: Throwable) { null }
                } else null

                if (cached != null) {
                    val cachedResult = SingleItemRecognitionResult(
                        name = cached.name,
                        category = cached.category,
                        suggestedLocation = cached.suggestedLocation,
                        quantity = cached.quantity,
                        unit = cached.unit,
                        tags = cached.tags.split(",").filter { it.isNotBlank() },
                        storageTips = cached.storageTips.ifBlank { "本地特征高速匹配成功 (秒级解析)" }
                    )
                    _aiState.value = AiOperationState.SingleSuccess(cachedResult, localPath)
                    return@launch
                }

                // Step 3: Progressive status hints during AI recognition
                val progressJob = launch {
                    _aiState.value = AiOperationState.Loading("✨ AI 正在极速连接视觉引擎...")
                    delay(1500)
                    _aiState.value = AiOperationState.Loading("🔍 正在智能分析物品特征与空间推荐...")
                    delay(2000)
                    _aiState.value = AiOperationState.Loading("⚡ 深度提取规格参数 (若超5秒将自动转本地解析)...")
                }

                // Step 4: Strict 5-second timeout for cloud AI recognition
                val aiTask = async(Dispatchers.IO) {
                    aiService.recognizeSingleItem(bitmap)
                }

                val result = withTimeoutOrNull(5000L) {
                    try {
                        aiTask.await()
                    } catch (e: Throwable) {
                        Result.failure(e)
                    }
                }
                progressJob.cancel()

                if (result != null && result.isSuccess) {
                    val res = result.getOrThrow()
                    _aiState.value = AiOperationState.SingleSuccess(res, localPath)
                    // Cache the recognized item for future instant retrieval
                    try {
                        if (hashKey != null) {
                            repository.saveAiCache(
                                AiRecognitionCache(
                                    featureKey = hashKey,
                                    name = res.name,
                                    category = res.category,
                                    suggestedLocation = res.suggestedLocation,
                                    quantity = res.quantity,
                                    unit = res.unit,
                                    tags = res.tags.joinToString(","),
                                    storageTips = res.storageTips
                                )
                            )
                        }
                    } catch (ignored: Throwable) {}
                } else {
                    // Timeout > 5s or Network Error -> Seamlessly fallback to local heuristic engine
                    val isTimeout = (result == null)
                    val local = LocalHeuristicEngine.recognizeSingleItem(bitmap)
                    val degradationTip = if (isTimeout) {
                        "⚠️ 云端AI识别响应超过5秒，已自动无缝切换至本地离线引擎，流程不中断"
                    } else {
                        val errMsg = result?.exceptionOrNull()?.message ?: "云端连接异常"
                        "⚠️ 云端接口异常 ($errMsg)，已自动切换至本地引擎秒级解析"
                    }
                    showMessage(degradationTip)
                    val finalResult = local.copy(
                        storageTips = "${local.storageTips} ($degradationTip)"
                    )
                    _aiState.value = AiOperationState.SingleSuccess(finalResult, localPath)
                }
            } catch (e: Throwable) {
                // Ultimate fallback for absolute safety
                val local = LocalHeuristicEngine.recognizeSingleItem(bitmap)
                _aiState.value = AiOperationState.SingleSuccess(local, null)
            }
        }
    }

    fun retryLastRecognition() {
        lastSingleBitmap?.let {
            recognizeSinglePhoto(it)
        }
    }

    fun retryLastBatchRecognition() {
        lastBatchBitmap?.let {
            recognizeBatchListPlusPhoto(it)
        }
    }

    fun suggestItemFromName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val currentConfig = _apiConfig.value
                if (currentConfig.apiKey.isBlank() && !currentConfig.useLocalOfflineEngine) {
                    showMessage("未设置云服务，请设置，当前已自动转为本地识图")
                }

                val trimmed = name.trim().lowercase()
                // Check Room cache first
                val cached = try { repository.getAiCache(trimmed) } catch (e: Throwable) { null }
                if (cached != null) {
                    val cachedResult = SingleItemRecognitionResult(
                        name = cached.name,
                        category = cached.category,
                        suggestedLocation = cached.suggestedLocation,
                        quantity = cached.quantity,
                        unit = cached.unit,
                        tags = cached.tags.split(",").filter { it.isNotBlank() },
                        storageTips = cached.storageTips.ifBlank { "已根据本地收纳常识库秒级联想" }
                    )
                    _aiState.value = AiOperationState.SingleSuccess(cachedResult)
                    return@launch
                }

                _aiState.value = AiOperationState.Loading("正在进行本地特征表与分类匹配...")
                val result = aiService.suggestForNameOnly(name, allItems.value)
                result.onSuccess { res ->
                    _aiState.value = AiOperationState.SingleSuccess(res)
                    try {
                        repository.saveAiCache(
                            AiRecognitionCache(
                                featureKey = trimmed,
                                name = res.name,
                                category = res.category,
                                suggestedLocation = res.suggestedLocation,
                                quantity = res.quantity,
                                unit = res.unit,
                                tags = res.tags.joinToString(","),
                                storageTips = res.storageTips
                            )
                        )
                    } catch (ignored: Throwable) {}
                }.onFailure {
                    _aiState.value = AiOperationState.Error(it.message ?: "建议生成失败")
                }
            } catch (e: Throwable) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "建议生成异常")
            }
        }
    }

    fun recognizeBatchListPlusPhoto(bitmap: Bitmap) {
        lastBatchBitmap = bitmap
        viewModelScope.launch {
            try {
                val currentConfig = _apiConfig.value
                if (currentConfig.apiKey.isBlank() && !currentConfig.useLocalOfflineEngine) {
                    showMessage("未设置云服务，请设置，当前已自动转为本地识图")
                }

                _aiState.value = AiOperationState.BatchStreaming(
                    items = emptyList(),
                    isComplete = false,
                    statusText = "ListPlus 正在识别物品全家福并拆分独立照片..."
                )

                val result = aiService.recognizeBatchListPlus(bitmap)
                result.onSuccess { rawList ->
                    // Step 2: Crop individual photos for each item in the batch safely
                    val processedList = rawList.mapIndexed { idx, item ->
                        val cropPath = try {
                            ImageCompressor.cropAndSaveItemThumbnail(
                                context = getApplication(),
                                sourceBitmap = bitmap,
                                boxNorm = item.boxNorm,
                                itemIndex = idx,
                                totalItems = rawList.size
                            )
                        } catch (e: Throwable) {
                            null
                        }
                        item.copy(imagePath = cropPath)
                    }

                    _aiState.value = AiOperationState.BatchStreaming(
                        items = processedList,
                        isComplete = true,
                        statusText = "识别完成，已为 ${processedList.size} 件物品拆分独立照片"
                    )
                    _aiState.value = AiOperationState.BatchSuccess(processedList)
                }.onFailure {
                    _aiState.value = AiOperationState.Error(it.message ?: "批量识图超时或失败")
                }
            } catch (e: Throwable) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "批量识图异常")
            }
        }
    }

    fun parseReceiptPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _aiState.value = AiOperationState.Loading("AI 正在扫描小票/面单商品明细...")
                val result = aiService.parseReceiptOrLabel(bitmap)
                result.onSuccess {
                    _aiState.value = AiOperationState.ReceiptSuccess(it)
                }.onFailure {
                    _aiState.value = AiOperationState.Error(it.message ?: "小票解析失败")
                }
            } catch (e: Throwable) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "小票解析异常")
            }
        }
    }

    fun executeSemanticSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                _aiState.value = AiOperationState.Loading("正在理解「$query」的语义并筛选物品...")
                val items = allItems.value
                val result = aiService.semanticSearch(query, items)
                result.onSuccess {
                    _semanticMatchedIds.value = it.matchedItemIds
                    _aiState.value = AiOperationState.SemanticSuccess(it)
                    showMessage("语义筛选出 ${it.matchedItemIds.size} 件物品: ${it.explanation}")
                }.onFailure {
                    _aiState.value = AiOperationState.Error(it.message ?: "语义检索失败")
                }
            } catch (e: Throwable) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "语义检索异常")
            }
        }
    }

    fun generateDeclutterRecommendations() {
        viewModelScope.launch {
            try {
                _aiState.value = AiOperationState.Loading("AI 正在分析闲置周期与临期状态，生成断舍离建议...")
                val items = allItems.value
                val result = aiService.generateDeclutterReport(items)
                result.onSuccess {
                    _aiState.value = AiOperationState.DeclutterSuccess(it)
                }.onFailure {
                    _aiState.value = AiOperationState.Error(it.message ?: "断舍离建议生成失败")
                }
            } catch (e: Throwable) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "断舍离建议生成异常")
            }
        }
    }

    // Barcode / QR Scan processing
    fun handleScannedCode(code: String, onNavigateToContainer: (Long) -> Unit) {
        viewModelScope.launch {
            // Check if it matches a container boxCode e.g. "BOX-2026-001"
            val container = repository.getContainerByCode(code)
            if (container != null) {
                showMessage("扫码成功：已精准定位到容器「${container.name}」")
                setSelectedContainerFilter(container.id)
                onNavigateToContainer(container.id)
                return@launch
            }

            // Check if it matches an item barcode
            val item = repository.getItemByBarcode(code)
            if (item != null) {
                showMessage("扫码定位到物品：「${item.name}」，位于 ${item.locationPath}")
                _searchQuery.value = item.name
                return@launch
            }

            // Unmatched barcode: set as search query and prompt user
            _searchQuery.value = code
            showMessage("扫描内容: $code (可在录入物品时关联为条形码)")
        }
    }

    // QR Code generation for container
    suspend fun generateContainerQrCode(boxCode: String): Bitmap {
        return QrCodeUtils.generateQrBitmap(boxCode, 600, 600)
    }

    // Backup & Restore
    fun exportBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = BackupManager.createBackupJson(getApplication())
            onReady(json)
            showMessage("数据备份文件生成完毕，包含所有物品及图片编码")
        }
    }

    fun restoreBackupJson(jsonString: String) {
        viewModelScope.launch {
            _restoreProgress.value = RestoreProgressState(isRestoring = true, stage = "正在准备恢复...")
            val result = BackupManager.restoreFromJson(getApplication(), jsonString) { current, total, stage ->
                _restoreProgress.value = RestoreProgressState(
                    isRestoring = true,
                    current = current,
                    total = total,
                    stage = stage
                )
            }
            _restoreProgress.value = RestoreProgressState(isRestoring = false)
            result.onSuccess {
                showMessage("瞬间无损恢复成功！恢复了 ${it.itemCount} 件物品，${it.containerCount} 个收纳箱")
            }.onFailure {
                showMessage("备份文件解析恢复失败: ${it.message}")
            }
        }
    }

    // WebDAV
    fun updateWebDavConfig(config: WebDavConfig) {
        _webDavConfig.value = config
        WebDavManager.saveConfig(getApplication(), config)
    }

    fun testWebDavConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = WebDavManager.testConnection(_webDavConfig.value)
            res.onSuccess {
                onResult(true, it)
                showMessage(it)
            }.onFailure {
                onResult(false, it.message ?: "连接失败")
                showMessage(it.message ?: "连接失败")
            }
        }
    }

    fun uploadToWebDav() {
        viewModelScope.launch {
            showMessage("正在打包数据并上传至 WebDAV 私有网盘...")
            val json = BackupManager.createBackupJson(getApplication())
            val res = WebDavManager.uploadBackup(getApplication(), _webDavConfig.value, json)
            res.onSuccess {
                _webDavConfig.value = WebDavManager.loadConfig(getApplication())
                showMessage(it)
            }.onFailure {
                showMessage(it.message ?: "上传失败")
            }
        }
    }

    fun downloadFromWebDav() {
        viewModelScope.launch {
            showMessage("正在从 WebDAV 拉取最新数据备份...")
            val res = WebDavManager.downloadBackup(_webDavConfig.value)
            res.onSuccess { json ->
                restoreBackupJson(json)
            }.onFailure {
                showMessage(it.message ?: "下载拉取失败")
            }
        }
    }

    // Security Lock
    fun updateSecuritySettings(settings: SecuritySettings) {
        _securitySettings.value = settings
        SecurityLockManager.saveSettings(getApplication(), settings)
        _isAppLocked.value = settings.isLockEnabled && settings.pinCode.isNotBlank()
        showMessage(if (settings.isLockEnabled) "隐私安全锁已开启" else "隐私安全锁已关闭")
    }

    fun unlockApp(pin: String): Boolean {
        if (!_securitySettings.value.isLockEnabled || _securitySettings.value.pinCode.isBlank()) {
            _isAppLocked.value = false
            return true
        }
        if (pin == _securitySettings.value.pinCode) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_securitySettings.value.isLockEnabled && _securitySettings.value.pinCode.isNotBlank()) {
            _isAppLocked.value = true
        }
    }

    // AI Config
    fun updateApiConfig(config: ApiConfig) {
        _apiConfig.value = config
        aiService.saveConfig(config)
        showMessage("API 配置已保存！")
    }

    fun toggleLocalOfflineEngine(enabled: Boolean) {
        val updated = _apiConfig.value.copy(useLocalOfflineEngine = enabled)
        _apiConfig.value = updated
        aiService.saveConfig(updated)
        if (enabled) {
            showMessage("已切换为「本地离线智能引擎」，无需配置 API Key，断网秒级可用！")
        } else {
            showMessage("已切换为「云端大模型引擎」")
        }
    }

    fun testAiApiConnection(config: ApiConfig, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _aiState.value = AiOperationState.Loading("正在测试 API 连通性与模型响应...")
            val res = aiService.testApiConnection(config)
            _aiState.value = AiOperationState.Idle
            res.onSuccess {
                onResult(true, it)
                showMessage(it)
            }.onFailure {
                val err = it.message ?: "连通测试失败"
                onResult(false, err)
                showMessage(err)
            }
        }
    }
}

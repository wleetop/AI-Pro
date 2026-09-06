package com.example.repository

import com.example.data.AiRecognitionCache
import com.example.data.LocationNode
import com.example.data.StorageCategory
import com.example.data.StorageContainer
import com.example.data.StorageDatabase
import com.example.data.StorageItem
import kotlinx.coroutines.flow.Flow

class StorageRepository(private val database: StorageDatabase) {

    val allActiveItems: Flow<List<StorageItem>> = database.storageItemDao().getAllActiveItems()
    val allContainers: Flow<List<StorageContainer>> = database.storageContainerDao().getAllContainers()
    val allCategories: Flow<List<StorageCategory>> = database.storageCategoryDao().getAllCategories()
    val allLocations: Flow<List<LocationNode>> = database.locationNodeDao().getAllNodes()

    fun searchItems(query: String): Flow<List<StorageItem>> {
        return database.storageItemDao().searchItems(query)
    }

    fun getItemsByContainer(containerId: Long): Flow<List<StorageItem>> {
        return database.storageItemDao().getItemsByContainer(containerId)
    }

    fun getExpiringItems(thresholdTime: Long): Flow<List<StorageItem>> {
        return database.storageItemDao().getExpiringItems(thresholdTime)
    }

    suspend fun getAllItemsSnapshot(): List<StorageItem> {
        return database.storageItemDao().getAllItemsSnapshot()
    }

    suspend fun getItemById(id: Long): StorageItem? {
        return database.storageItemDao().getItemById(id)
    }

    suspend fun getItemByBarcode(barcode: String): StorageItem? {
        return database.storageItemDao().getItemByBarcode(barcode)
    }

    suspend fun getContainerByCode(boxCode: String): StorageContainer? {
        return database.storageContainerDao().getContainerByCode(boxCode)
    }

    suspend fun insertItem(item: StorageItem): Long {
        return database.storageItemDao().insertItem(item)
    }

    suspend fun insertItems(items: List<StorageItem>) {
        database.storageItemDao().insertAll(items)
    }

    suspend fun updateItem(item: StorageItem) {
        database.storageItemDao().updateItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteItem(item: StorageItem) {
        database.storageItemDao().deleteItem(item)
    }

    suspend fun deleteItemById(id: Long) {
        database.storageItemDao().deleteItemById(id)
    }

    suspend fun deleteItemsByIds(ids: List<Long>) {
        database.storageItemDao().deleteItemsByIds(ids)
    }

    suspend fun batchMoveItems(ids: List<Long>, containerId: Long?, locationPath: String) {
        database.storageItemDao().batchMoveItems(ids, containerId, locationPath)
    }

    suspend fun batchUpdateCategory(ids: List<Long>, category: String) {
        database.storageItemDao().batchUpdateCategory(ids, category)
    }

    suspend fun insertContainer(container: StorageContainer): Long {
        return database.storageContainerDao().insertContainer(container)
    }

    suspend fun updateContainer(container: StorageContainer) {
        database.storageContainerDao().updateContainer(container)
    }

    suspend fun deleteContainer(container: StorageContainer) {
        database.storageContainerDao().deleteContainer(container)
    }

    suspend fun insertCategory(category: StorageCategory): Long {
        return database.storageCategoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(category: StorageCategory) {
        database.storageCategoryDao().deleteCategory(category)
    }

    suspend fun insertLocationNode(node: LocationNode): Long {
        return database.locationNodeDao().insertNode(node)
    }

    suspend fun deleteLocationNode(node: LocationNode) {
        database.locationNodeDao().deleteNode(node)
    }

    // AI Cache operations
    suspend fun getAiCache(key: String): AiRecognitionCache? {
        val direct = database.aiRecognitionCacheDao().getCacheByKey(key)
        if (direct != null) {
            database.aiRecognitionCacheDao().recordHit(key)
            return direct
        }
        val similar = database.aiRecognitionCacheDao().findSimilarCache(key)
        if (similar != null) {
            database.aiRecognitionCacheDao().recordHit(similar.featureKey)
            return similar
        }
        return null
    }

    suspend fun saveAiCache(cache: AiRecognitionCache) {
        database.aiRecognitionCacheDao().insertOrUpdate(cache)
    }

    suspend fun clearAiCache() {
        database.aiRecognitionCacheDao().clearCache()
    }
}

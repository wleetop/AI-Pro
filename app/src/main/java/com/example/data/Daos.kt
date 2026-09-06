package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageItemDao {
    @Query("SELECT * FROM storage_items WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveItems(): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items ORDER BY updatedAt DESC")
    suspend fun getAllItemsSnapshot(): List<StorageItem>

    @Query("SELECT * FROM storage_items WHERE id = :id")
    suspend fun getItemById(id: Long): StorageItem?

    @Query("SELECT * FROM storage_items WHERE containerId = :containerId AND isArchived = 0")
    fun getItemsByContainer(containerId: Long): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE category = :category AND isArchived = 0")
    fun getItemsByCategory(category: String): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE (name LIKE '%' || :query || '%' OR locationPath LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND isArchived = 0")
    fun searchItems(query: String): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE expiryDate IS NOT NULL AND expiryDate <= :thresholdTime AND isArchived = 0 ORDER BY expiryDate ASC")
    fun getExpiringItems(thresholdTime: Long): Flow<List<StorageItem>>

    @Query("SELECT * FROM storage_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): StorageItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StorageItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StorageItem>)

    @Update
    suspend fun updateItem(item: StorageItem)

    @Delete
    suspend fun deleteItem(item: StorageItem)

    @Query("DELETE FROM storage_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM storage_items WHERE id IN (:ids)")
    suspend fun deleteItemsByIds(ids: List<Long>)

    @Query("UPDATE storage_items SET containerId = :containerId, locationPath = :locationPath, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun batchMoveItems(ids: List<Long>, containerId: Long?, locationPath: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE storage_items SET category = :category, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun batchUpdateCategory(ids: List<Long>, category: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM storage_items")
    suspend fun clearAll()
}

@Dao
interface StorageContainerDao {
    @Query("SELECT * FROM storage_containers ORDER BY id DESC")
    fun getAllContainers(): Flow<List<StorageContainer>>

    @Query("SELECT * FROM storage_containers")
    suspend fun getAllContainersSnapshot(): List<StorageContainer>

    @Query("SELECT * FROM storage_containers WHERE id = :id")
    suspend fun getContainerById(id: Long): StorageContainer?

    @Query("SELECT * FROM storage_containers WHERE boxCode = :boxCode LIMIT 1")
    suspend fun getContainerByCode(boxCode: String): StorageContainer?

    @Query("SELECT * FROM storage_containers WHERE nfcTagId = :tagId LIMIT 1")
    suspend fun getContainerByNfcTag(tagId: String): StorageContainer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: StorageContainer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(containers: List<StorageContainer>)

    @Update
    suspend fun updateContainer(container: StorageContainer)

    @Delete
    suspend fun deleteContainer(container: StorageContainer)

    @Query("DELETE FROM storage_containers")
    suspend fun clearAll()
}

@Dao
interface StorageCategoryDao {
    @Query("SELECT * FROM storage_categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<StorageCategory>>

    @Query("SELECT * FROM storage_categories")
    suspend fun getAllCategoriesSnapshot(): List<StorageCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: StorageCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<StorageCategory>)

    @Delete
    suspend fun deleteCategory(category: StorageCategory)

    @Query("DELETE FROM storage_categories")
    suspend fun clearAll()
}

@Dao
interface LocationNodeDao {
    @Query("SELECT * FROM location_nodes ORDER BY id ASC")
    fun getAllNodes(): Flow<List<LocationNode>>

    @Query("SELECT * FROM location_nodes")
    suspend fun getAllNodesSnapshot(): List<LocationNode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: LocationNode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<LocationNode>)

    @Delete
    suspend fun deleteNode(node: LocationNode)

    @Query("DELETE FROM location_nodes")
    suspend fun clearAll()
}

@Dao
interface AiRecognitionCacheDao {
    @Query("SELECT * FROM ai_recognition_cache WHERE featureKey = :key LIMIT 1")
    suspend fun getCacheByKey(key: String): AiRecognitionCache?

    @Query("SELECT * FROM ai_recognition_cache WHERE featureKey LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findSimilarCache(query: String): AiRecognitionCache?

    @Query("SELECT * FROM ai_recognition_cache ORDER BY hitCount DESC LIMIT 50")
    suspend fun getTopCachedItems(): List<AiRecognitionCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cache: AiRecognitionCache)

    @Query("UPDATE ai_recognition_cache SET hitCount = hitCount + 1, updatedAt = :now WHERE featureKey = :key")
    suspend fun recordHit(key: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM ai_recognition_cache")
    suspend fun clearCache()
}

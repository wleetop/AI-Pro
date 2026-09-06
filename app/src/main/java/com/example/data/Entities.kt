package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_items")
data class StorageItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "日常居家",
    val locationPath: String = "未分类位置",
    val containerId: Long? = null,
    val quantity: Int = 1,
    val unit: String = "件",
    val price: Double? = null,
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null,
    val imageUri: String? = null,
    val barcode: String? = null,
    val notes: String? = null,
    val tags: String = "",
    val isStarred: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "storage_containers")
data class StorageContainer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val locationPath: String = "储物间",
    val boxCode: String,
    val iconName: String = "box",
    val colorHex: String = "#3B82F6",
    val capacityLimit: Int = 0,
    val notes: String = "",
    val nfcTagId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "storage_categories")
data class StorageCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "category",
    val colorHex: String = "#3B82F6",
    val isCustom: Boolean = false
)

@Entity(tableName = "location_nodes")
data class LocationNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val icon: String = "room"
)

@Entity(tableName = "ai_recognition_cache")
data class AiRecognitionCache(
    @PrimaryKey val featureKey: String, // Normalized name, barcode, or image hash
    val name: String,
    val category: String,
    val suggestedLocation: String,
    val quantity: Int = 1,
    val unit: String = "件",
    val tags: String = "",
    val storageTips: String = "",
    val hitCount: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

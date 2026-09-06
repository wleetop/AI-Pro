package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.data.LocationNode
import com.example.data.StorageCategory
import com.example.data.StorageContainer
import com.example.data.StorageDatabase
import com.example.data.StorageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    data class BackupMetadata(
        val appVersion: String = "1.0",
        val timestamp: Long = System.currentTimeMillis(),
        val itemCount: Int,
        val containerCount: Int,
        val categoryCount: Int
    )

    suspend fun createBackupJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = StorageDatabase.getInstance(context)
        val items = db.storageItemDao().getAllItemsSnapshot()
        val containers = db.storageContainerDao().getAllContainersSnapshot()
        val categories = db.storageCategoryDao().getAllCategoriesSnapshot()
        val locations = db.locationNodeDao().getAllNodesSnapshot()

        val rootJson = JSONObject()
        rootJson.put("appName", "万物收纳 Pro")
        rootJson.put("version", "1.0")
        rootJson.put("timestamp", System.currentTimeMillis())

        // Containers
        val containersArray = JSONArray()
        containers.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("locationPath", c.locationPath)
            obj.put("boxCode", c.boxCode)
            obj.put("iconName", c.iconName)
            obj.put("colorHex", c.colorHex)
            obj.put("capacityLimit", c.capacityLimit)
            obj.put("notes", c.notes)
            obj.put("nfcTagId", c.nfcTagId ?: "")
            containersArray.put(obj)
        }
        rootJson.put("containers", containersArray)

        // Categories
        val categoriesArray = JSONArray()
        categories.forEach { cat ->
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("icon", cat.icon)
            obj.put("colorHex", cat.colorHex)
            obj.put("isCustom", cat.isCustom)
            categoriesArray.put(obj)
        }
        rootJson.put("categories", categoriesArray)

        // Locations
        val locationsArray = JSONArray()
        locations.forEach { loc ->
            val obj = JSONObject()
            obj.put("id", loc.id)
            obj.put("name", loc.name)
            obj.put("parentId", loc.parentId ?: -1L)
            obj.put("icon", loc.icon)
            locationsArray.put(obj)
        }
        rootJson.put("locations", locationsArray)

        // Items + Optional image base64 if small
        val itemsArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("locationPath", item.locationPath)
            obj.put("containerId", item.containerId ?: -1L)
            obj.put("quantity", item.quantity)
            obj.put("unit", item.unit)
            if (item.price != null) obj.put("price", item.price)
            if (item.purchaseDate != null) obj.put("purchaseDate", item.purchaseDate)
            if (item.expiryDate != null) obj.put("expiryDate", item.expiryDate)
            obj.put("barcode", item.barcode ?: "")
            obj.put("notes", item.notes ?: "")
            obj.put("tags", item.tags)
            obj.put("isStarred", item.isStarred)
            obj.put("isArchived", item.isArchived)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)

            // Pack image as base64 if it exists locally
            if (!item.imageUri.isNullOrBlank()) {
                val file = File(item.imageUri)
                if (file.exists() && file.length() < 2 * 1024 * 1024) { // max 2MB in backup
                    try {
                        val bmp = BitmapFactory.decodeFile(file.absolutePath)
                        if (bmp != null) {
                            val baos = ByteArrayOutputStream()
                            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                            obj.put("imageBase64", b64)
                        }
                    } catch (e: Exception) {
                        // ignore image encoding errors
                    }
                }
            }
            itemsArray.put(obj)
        }
        rootJson.put("items", itemsArray)

        rootJson.toString(2)
    }

    suspend fun restoreFromJson(
        context: Context,
        jsonString: String,
        onProgress: (current: Int, total: Int, stage: String) -> Unit = { _, _, _ -> }
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, 100, "正在解析备份文件结构...")
            val root = JSONObject(jsonString)
            val db = StorageDatabase.getInstance(context)

            // Parse Containers
            val containersList = mutableListOf<StorageContainer>()
            val containersArray = root.optJSONArray("containers")
            if (containersArray != null) {
                for (i in 0 until containersArray.length()) {
                    val obj = containersArray.getJSONObject(i)
                    containersList.add(
                        StorageContainer(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            locationPath = obj.optString("locationPath", "储物间"),
                            boxCode = obj.getString("boxCode"),
                            iconName = obj.optString("iconName", "box"),
                            colorHex = obj.optString("colorHex", "#3B82F6"),
                            capacityLimit = obj.optInt("capacityLimit", 0),
                            notes = obj.optString("notes", ""),
                            nfcTagId = obj.optString("nfcTagId").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            // Parse Categories
            val categoriesList = mutableListOf<StorageCategory>()
            val categoriesArray = root.optJSONArray("categories")
            if (categoriesArray != null) {
                for (i in 0 until categoriesArray.length()) {
                    val obj = categoriesArray.getJSONObject(i)
                    categoriesList.add(
                        StorageCategory(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            icon = obj.optString("icon", "category"),
                            colorHex = obj.optString("colorHex", "#3B82F6"),
                            isCustom = obj.optBoolean("isCustom", false)
                        )
                    )
                }
            }

            // Parse Locations
            val locationsList = mutableListOf<LocationNode>()
            val locationsArray = root.optJSONArray("locations")
            if (locationsArray != null) {
                for (i in 0 until locationsArray.length()) {
                    val obj = locationsArray.getJSONObject(i)
                    val parentIdVal = obj.optLong("parentId", -1L)
                    locationsList.add(
                        LocationNode(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            parentId = if (parentIdVal > 0) parentIdVal else null,
                            icon = obj.optString("icon", "room")
                        )
                    )
                }
            }

            // Parse Items with progress
            val imagesDir = File(context.filesDir, "item_images").apply { if (!exists()) mkdirs() }
            val itemsList = mutableListOf<StorageItem>()
            val itemsArray = root.optJSONArray("items")
            val totalItems = itemsArray?.length() ?: 0

            if (itemsArray != null) {
                for (i in 0 until totalItems) {
                    if (i % 10 == 0 || i == totalItems - 1) {
                        onProgress(i + 1, totalItems, "正在解码物品与图片 (${i + 1}/$totalItems)...")
                    }

                    val obj = itemsArray.getJSONObject(i)
                    val containerIdVal = obj.optLong("containerId", -1L)

                    var restoredImagePath: String? = null
                    val b64 = obj.optString("imageBase64", "")
                    if (b64.isNotBlank()) {
                        try {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            val destFile = File(imagesDir, "restored_${System.currentTimeMillis()}_$i.jpg")
                            FileOutputStream(destFile).use { fos ->
                                fos.write(bytes)
                                fos.flush()
                            }
                            restoredImagePath = destFile.absolutePath
                        } catch (e: Exception) {
                            // ignore image write error
                        }
                    }

                    itemsList.add(
                        StorageItem(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            category = obj.optString("category", "日常居家"),
                            locationPath = obj.optString("locationPath", "未分类位置"),
                            containerId = if (containerIdVal > 0) containerIdVal else null,
                            quantity = obj.optInt("quantity", 1),
                            unit = obj.optString("unit", "件"),
                            price = if (obj.has("price")) obj.optDouble("price") else null,
                            purchaseDate = if (obj.has("purchaseDate")) obj.optLong("purchaseDate") else null,
                            expiryDate = if (obj.has("expiryDate")) obj.optLong("expiryDate") else null,
                            imageUri = restoredImagePath,
                            barcode = obj.optString("barcode").takeIf { it.isNotBlank() },
                            notes = obj.optString("notes").takeIf { it.isNotBlank() },
                            tags = obj.optString("tags", ""),
                            isStarred = obj.optBoolean("isStarred", false),
                            isArchived = obj.optBoolean("isArchived", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Write into DB in chunks with transactions to avoid thread stalling
            onProgress(totalItems, totalItems, "正在批量写入本地安全数据库...")
            if (categoriesList.isNotEmpty()) {
                db.storageCategoryDao().clearAll()
                categoriesList.chunked(100).forEach { chunk ->
                    db.storageCategoryDao().insertAll(chunk)
                }
            }
            if (containersList.isNotEmpty()) {
                db.storageContainerDao().clearAll()
                containersList.chunked(100).forEach { chunk ->
                    db.storageContainerDao().insertAll(chunk)
                }
            }
            if (locationsList.isNotEmpty()) {
                db.locationNodeDao().clearAll()
                locationsList.chunked(100).forEach { chunk ->
                    db.locationNodeDao().insertAll(chunk)
                }
            }
            if (itemsList.isNotEmpty()) {
                db.storageItemDao().clearAll()
                itemsList.chunked(100).forEach { chunk ->
                    db.storageItemDao().insertAll(chunk)
                }
            }

            onProgress(totalItems, totalItems, "恢复完成！")

            Result.success(
                BackupMetadata(
                    itemCount = itemsList.size,
                    containerCount = containersList.size,
                    categoryCount = categoriesList.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

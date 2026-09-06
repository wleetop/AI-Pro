package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageCompressor {

    /**
     * Safely decodes a Uri into a software-renderable, downsampled Bitmap on Dispatchers.IO.
     * Guaranteed to never return HARDWARE bitmaps and never throw unhandled exceptions.
     */
    fun decodeUriToBitmap(context: Context, uri: Uri, maxDimension: Int = 1280): Bitmap? {
        val decodedBmp = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                    val size = info.size
                    if (size.width > maxDimension || size.height > maxDimension) {
                        val scale = if (size.width > size.height) {
                            maxDimension.toFloat() / size.width
                        } else {
                            maxDimension.toFloat() / size.height
                        }
                        val targetW = (size.width * scale).toInt().coerceAtLeast(1)
                        val targetH = (size.height * scale).toInt().coerceAtLeast(1)
                        decoder.setTargetSize(targetW, targetH)
                    }
                }
            } else {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, boundsOptions)
                inputStream.close()

                var sampleSize = 1
                while (boundsOptions.outWidth / sampleSize > maxDimension || boundsOptions.outHeight / sampleSize > maxDimension) {
                    sampleSize *= 2
                }

                val decodeStream = context.contentResolver.openInputStream(uri) ?: return null
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bmp = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
                decodeStream.close()
                bmp
            }
        } catch (e: Throwable) {
            try {
                @Suppress("DEPRECATION")
                val fallbackBmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                ensureSoftwareBitmap(fallbackBmp)
            } catch (ignored: Throwable) {
                null
            }
        } ?: return null

        val safeBmp = ensureSoftwareBitmap(decodedBmp)

        // 读取 Exif 旋转标志，确保 Bitmap 方向与直观视觉 100% 正向对齐
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                rotateBitmapIfNeeded(safeBmp, orientation)
            } ?: safeBmp
        } catch (e: Throwable) {
            safeBmp
        }
    }

    /**
     * Ensures bitmap is mutable / software-renderable (ARGB_8888) so it can safely be drawn to Canvas
     * without HardwareBitmap crashes.
     */
    fun ensureSoftwareBitmap(source: Bitmap): Bitmap {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && source.config == Bitmap.Config.HARDWARE) {
                source.copy(Bitmap.Config.ARGB_8888, false) ?: source
            } else {
                source
            }
        } catch (e: Throwable) {
            source
        }
    }

    /**
     * Canvas-based image pre-processor: scales and compresses bitmap with Canvas rendering,
     * exporting WebP (or JPEG fallback) bytes for AI payload optimization.
     */
    fun preprocessBitmapForAi(
        source: Bitmap,
        maxDimension: Int = 640,
        quality: Int = 75,
        useWebP: Boolean = true
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            val safeSource = ensureSoftwareBitmap(source)
            val width = safeSource.width
            val height = safeSource.height
            val scale = if (width > maxDimension || height > maxDimension) {
                if (width > height) maxDimension.toFloat() / width else maxDimension.toFloat() / height
            } else {
                1.0f
            }

            val targetW = (width * scale).toInt().coerceAtLeast(1)
            val targetH = (height * scale).toInt().coerceAtLeast(1)

            val targetBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(targetBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val srcRect = Rect(0, 0, width, height)
            val dstRect = RectF(0f, 0f, targetW.toFloat(), targetH.toFloat())
            canvas.drawBitmap(safeSource, srcRect, dstRect, paint)

            if (useWebP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    targetBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, baos)
                } else {
                    @Suppress("DEPRECATION")
                    targetBitmap.compress(Bitmap.CompressFormat.WEBP, quality, baos)
                }
            } else {
                targetBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }
            return baos.toByteArray()
        } catch (e: Throwable) {
            baos.reset()
            try {
                source.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            } catch (ignored: Throwable) {}
            return baos.toByteArray()
        }
    }

    /**
     * Crops a bounding sub-region or sub-box from the parent full photo safely,
     * and saves it as an independent standalone image file for individual item display.
     */
    suspend fun cropAndSaveItemThumbnail(
        context: Context,
        sourceBitmap: Bitmap,
        boxNorm: List<Int>?, // [ymin, xmin, ymax, xmax] in 0..1000 scale
        itemIndex: Int = 0,
        totalItems: Int = 1
    ): String = withContext(Dispatchers.IO) {
        try {
            val safeSource = ensureSoftwareBitmap(sourceBitmap)
            val width = safeSource.width
            val height = safeSource.height

            val cropRect: Rect = if (boxNorm != null && boxNorm.size == 4) {
                var rawYmin = (boxNorm[0] / 1000f * height).toInt()
                var rawXmin = (boxNorm[1] / 1000f * width).toInt()
                var rawYmax = (boxNorm[2] / 1000f * height).toInt()
                var rawXmax = (boxNorm[3] / 1000f * width).toInt()

                if (rawYmin > rawYmax) { val tmp = rawYmin; rawYmin = rawYmax; rawYmax = tmp }
                if (rawXmin > rawXmax) { val tmp = rawXmin; rawXmin = rawXmax; rawXmax = tmp }

                val boxW = (rawXmax - rawXmin).coerceAtLeast(1)
                val boxH = (rawYmax - rawYmin).coerceAtLeast(1)

                // 智能外扩 12% 缓冲区，防止边缘被裁剪
                val padX = (boxW * 0.12f).toInt().coerceAtLeast(8)
                val padY = (boxH * 0.12f).toInt().coerceAtLeast(8)

                var paddedXmin = rawXmin - padX
                var paddedXmax = rawXmax + padX
                var paddedYmin = rawYmin - padY
                var paddedYmax = rawYmax + padY

                // 长宽比修正：极细长或极扁的框水平/垂直适度填充
                val curW = paddedXmax - paddedXmin
                val curH = paddedYmax - paddedYmin
                val aspect = curW.toFloat() / curH.toFloat()
                if (aspect < 0.5f) {
                    val targetW = (curH * 0.75f).toInt()
                    val diff = (targetW - curW) / 2
                    paddedXmin -= diff
                    paddedXmax += diff
                } else if (aspect > 2.0f) {
                    val targetH = (curW * 0.75f).toInt()
                    val diff = (targetH - curH) / 2
                    paddedYmin -= diff
                    paddedYmax += diff
                }

                val finalXmin = paddedXmin.coerceIn(0, (width - 1).coerceAtLeast(0))
                val finalYmin = paddedYmin.coerceIn(0, (height - 1).coerceAtLeast(0))
                val finalXmax = paddedXmax.coerceIn(finalXmin + 1, width)
                val finalYmax = paddedYmax.coerceIn(finalYmin + 1, height)

                Rect(finalXmin, finalYmin, finalXmax, finalYmax)
            } else {
                // 无坐标框时保留全家福主图，严禁盲目切成块导致截取墙面木纹
                Rect(0, 0, width, height)
            }

            val safeX = cropRect.left.coerceIn(0, (width - 1).coerceAtLeast(0))
            val safeY = cropRect.top.coerceIn(0, (height - 1).coerceAtLeast(0))
            val maxAllowedW = (width - safeX).coerceAtLeast(1)
            val maxAllowedH = (height - safeY).coerceAtLeast(1)
            val croppedW = cropRect.width().coerceIn(1, maxAllowedW)
            val croppedH = cropRect.height().coerceIn(1, maxAllowedH)

            val cropped = Bitmap.createBitmap(safeSource, safeX, safeY, croppedW, croppedH)
            compressBitmapAndSave(context, cropped)
        } catch (e: Throwable) {
            compressBitmapAndSave(context, sourceBitmap)
        }
    }

    /**
     * 根据在全景图上点击的归一化中心点 (0.0..1.0)，实时以该点为中心智能裁切物品照片
     */
    suspend fun cropByCenterPoint(
        context: Context,
        sourceBitmap: Bitmap,
        normalizedX: Float, // 0.0f .. 1.0f
        normalizedY: Float, // 0.0f .. 1.0f
        cropRadiusRatio: Float = 0.18f
    ): String = withContext(Dispatchers.IO) {
        try {
            val safeSource = ensureSoftwareBitmap(sourceBitmap)
            val width = safeSource.width
            val height = safeSource.height

            val centerX = (normalizedX * width).toInt()
            val centerY = (normalizedY * height).toInt()

            val halfW = (width * cropRadiusRatio).toInt().coerceAtLeast(60)
            val halfH = (height * cropRadiusRatio).toInt().coerceAtLeast(60)

            val xmin = (centerX - halfW).coerceIn(0, (width - 1).coerceAtLeast(0))
            val ymin = (centerY - halfH).coerceIn(0, (height - 1).coerceAtLeast(0))
            val xmax = (centerX + halfW).coerceIn(xmin + 1, width)
            val ymax = (centerY + halfH).coerceIn(ymin + 1, height)

            val croppedW = xmax - xmin
            val croppedH = ymax - ymin

            val cropped = Bitmap.createBitmap(safeSource, xmin, ymin, croppedW, croppedH)
            compressBitmapAndSave(context, cropped)
        } catch (e: Throwable) {
            compressBitmapAndSave(context, sourceBitmap)
        }
    }

    suspend fun compressAndSaveImage(context: Context, sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val originalBitmap = decodeUriToBitmap(context, sourceUri)
        if (originalBitmap == null) {
            return@withContext sourceUri.toString()
        }

        // Apply rotation if needed from exif
        val rotatedBitmap = try {
            val exifStream = context.contentResolver.openInputStream(sourceUri)
            if (exifStream != null) {
                val exif = ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exifStream.close()
                rotateBitmapIfNeeded(originalBitmap, orientation)
            } else {
                originalBitmap
            }
        } catch (e: Throwable) {
            originalBitmap
        }

        compressBitmapAndSave(context, rotatedBitmap)
    }

    suspend fun compressBitmapAndSave(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val safeBmp = ensureSoftwareBitmap(bitmap)
        val maxDim = 960
        val width = safeBmp.width
        val height = safeBmp.height
        val scaledBitmap = if (width > maxDim || height > maxDim) {
            val scale = if (width > height) maxDim.toFloat() / width else maxDim.toFloat() / height
            val targetWidth = (width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(safeBmp, targetWidth, targetHeight, true)
        } else {
            safeBmp
        }

        val imagesDir = File(context.filesDir, "item_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, "item_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
            out.flush()
        }
        file.absolutePath
    }

    suspend fun saveCompressedBitmap(
        context: Context,
        sourceBitmap: Bitmap,
        maxDimension: Int = 960,
        quality: Int = 80
    ): String = withContext(Dispatchers.IO) {
        val safeBmp = ensureSoftwareBitmap(sourceBitmap)
        val width = safeBmp.width
        val height = safeBmp.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val scale = if (width > height) maxDimension.toFloat() / width else maxDimension.toFloat() / height
            val targetWidth = (width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(safeBmp, targetWidth, targetHeight, true)
        } else {
            safeBmp
        }

        val imagesDir = File(context.filesDir, "item_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, "med_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.flush()
        }
        file.absolutePath
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

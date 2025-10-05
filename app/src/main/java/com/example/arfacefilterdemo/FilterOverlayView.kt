package com.example.arfacefilterdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs

class FilterOverlayView(
    context: Context,
    private val face: Face,
    private val transformData: FloatArray // [scaleX, scaleY, flipX, translateX, viewWidth, viewHeight]
) : View(context) {
    private var currentFilter: String = "none"
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val TAG = "FilterOverlayView"
    private val bitmapCache = mutableMapOf<String, Bitmap>()

    init {
        // Tải và nén bitmap trước, di chuyển sang thread phụ
        Thread {
            bitmapCache["sunglasses"] = BitmapFactory.decodeResource(resources, R.drawable.sunglasses)
                .let { Bitmap.createScaledBitmap(it, it.width / 4, it.height / 4, true) }
            bitmapCache["cat_ears"] = BitmapFactory.decodeResource(resources, R.drawable.cat_ears)
                .let { Bitmap.createScaledBitmap(it, it.width / 4, it.height / 4, true) }
            bitmapCache["hat"] = BitmapFactory.decodeResource(resources, R.drawable.hat)
                .let { Bitmap.createScaledBitmap(it, it.width / 4, it.height / 4, true) }
        }.start()
    }

    fun setFilter(filterId: String) {
        currentFilter = filterId
        Log.d(TAG, "Setting filter: $currentFilter")
        invalidate() // Đảm bảo re-render
    }

    private fun mapX(x: Float): Float {
        val scaleX = transformData[0]
        val flipX = transformData[2]
        val translateX = transformData[3]
        return (x * flipX * scaleX) + translateX
    }

    private fun mapY(y: Float): Float {
        val scaleY = transformData[1]
        return y * scaleY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentFilter == "none" || !bitmapCache.containsKey(currentFilter)) return

        val bounds = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)?.position

        val mappedLeft = mapX(bounds.left.toFloat())
        val mappedTop = mapY(bounds.top.toFloat())
        val mappedRight = mapX(bounds.right.toFloat())
        val mappedBottom = mapY(bounds.bottom.toFloat())
        val faceWidth = abs(mappedRight - mappedLeft)
        val faceHeight = abs(mappedBottom - mappedTop)

        canvas.saveLayer(null, null)
        when (currentFilter) {
            "sunglasses" -> {
                if (leftEye != null && rightEye != null) {
                    val mappedLeftEyeX = mapX(leftEye.x)
                    val mappedRightEyeX = mapX(rightEye.x)
                    val mappedEyeY = mapY((leftEye.y + rightEye.y) / 2f)

                    val anchorX = (mappedLeftEyeX + mappedRightEyeX) / 2f
                    val anchorY = mappedEyeY + (faceHeight * 0.05f) // Điều chỉnh dựa trên chiều cao khuôn mặt
                    val scale = faceWidth / bitmapCache[currentFilter]!!.width.toFloat() * 1.2f // Tăng scale cho vừa khuôn mặt

                    canvas.save()
                    canvas.translate(anchorX, anchorY)
                    canvas.scale(scale, scale)
                    canvas.drawBitmap(bitmapCache[currentFilter]!!, -bitmapCache[currentFilter]!!.width / 2f, -bitmapCache[currentFilter]!!.height / 2f, paint)
                    canvas.restore()
                    Log.d(TAG, "Drawing sunglasses at ($anchorX, $anchorY) with scale $scale")
                }
            }
            "cat_ears" -> {
                val anchorX = (mappedLeft + mappedRight) / 2f
                val anchorY = mappedTop - (faceHeight * 0.2f) // Điều chỉnh dựa trên chiều cao khuôn mặt
                val scale = faceWidth / bitmapCache[currentFilter]!!.width.toFloat() * 1.5f // Tăng scale

                canvas.save()
                canvas.translate(anchorX, anchorY)
                canvas.scale(scale, scale)
                canvas.drawBitmap(bitmapCache[currentFilter]!!, -bitmapCache[currentFilter]!!.width / 2f, -bitmapCache[currentFilter]!!.height / 2f, paint)
                canvas.restore()
                Log.d(TAG, "Drawing cat_ears at ($anchorX, $anchorY) with scale $scale")
            }
            "hat" -> {
                if (noseBase != null) {
                    val anchorX = mapX(noseBase.x)
                    val anchorY = mappedTop - (faceHeight * 0.3f) // Điều chỉnh dựa trên chiều cao khuôn mặt
                    val scale = faceWidth / bitmapCache[currentFilter]!!.width.toFloat() * 1.4f // Tăng scale

                    canvas.save()
                    canvas.translate(anchorX, anchorY)
                    canvas.scale(scale, scale)
                    canvas.drawBitmap(bitmapCache[currentFilter]!!, -bitmapCache[currentFilter]!!.width / 2f, -bitmapCache[currentFilter]!!.height / 2f, paint)
                    canvas.restore()
                    Log.d(TAG, "Drawing hat at ($anchorX, $anchorY) with scale $scale")
                }
            }
        }
        canvas.restore()
    }
}
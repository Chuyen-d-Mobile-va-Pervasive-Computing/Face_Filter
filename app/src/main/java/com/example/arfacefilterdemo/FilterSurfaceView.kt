package com.example.arfacefilterdemo

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

class FilterSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var faces: List<Face> = emptyList()
    private var currentFilter = "none"
    private var isFrontCamera = true
    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()
    private var filterResMap: Map<String, Int> = mapOf()
    private var drawJob: Job? = null
    private var isProcessingFrame = false
    private val TAG = "FilterSurfaceView"

    init {
        holder.addCallback(this)
    }

    fun updateFilter(filter: String) {
        currentFilter = filter
        if (holder.surface.isValid) drawFrame()
        Log.d(TAG, "Filter updated to: $currentFilter")
    }

    fun updateFaces(newFaces: List<Face>, filter: String, filters: Map<String, Int>, frontCamera: Boolean) {
        faces = newFaces
        currentFilter = filter
        filterResMap = filters
        isFrontCamera = frontCamera
        if (holder.surface.isValid) drawFrame()
        Log.d(TAG, "Faces updated: ${faces.size}, Filter: $currentFilter")
    }

    fun isProcessingFrame(): Boolean = drawJob?.isActive == true

    private fun drawFrame() {
        drawJob?.cancel()
        drawJob = CoroutineScope(Dispatchers.Default).launch {
            val canvas = holder.lockCanvas() ?: return@launch
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            Log.d(TAG, "Drawing frame with filter: $currentFilter, Faces: ${faces.size}")
            if (currentFilter != "none" && faces.isNotEmpty()) {
                val bmp = getFilterBitmap(currentFilter) ?: run {
                    Log.e(TAG, "Failed to get bitmap for filter: $currentFilter")
                    return@launch
                }
                for (face in faces.take(1)) {
                    drawFilterOnFace(canvas, bmp, face)
                }
            }
            holder.unlockCanvasAndPost(canvas)
            isProcessingFrame = false
        }
    }

    private fun getFilterBitmap(name: String): Bitmap? {
        bitmapCache[name]?.let { return it }
        val resId = filterResMap[name] ?: run {
            Log.e(TAG, "No resource ID for filter: $name")
            return null
        }
        try {
            val bmp = BitmapFactory.decodeResource(resources, resId)
            val scaled = Bitmap.createScaledBitmap(bmp, bmp.width / 3, bmp.height / 3, true)
            bitmapCache[name] = scaled
            Log.d(TAG, "Loaded bitmap for filter: $name")
            return scaled
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap for filter: $name", e)
            return null
        }
    }

    private fun drawFilterOnFace(canvas: Canvas, bmp: Bitmap, face: Face) {
        val bounds = face.boundingBox
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        val width = width.toFloat()
        val height = height.toFloat()
        val scaleX = width / bounds.width().toFloat()
        val scaleY = height / bounds.height().toFloat()
        val flipX = if (isFrontCamera) -1f else 1f
        val translateX = if (isFrontCamera) width else 0f
        val mappedLeft = (bounds.left * scaleX * flipX) + translateX
        val mappedTop = bounds.top * scaleY
        val mappedRight = (bounds.right * scaleX * flipX) + translateX
        val mappedBottom = bounds.bottom * scaleY
        val faceWidth = abs(mappedRight - mappedLeft)
        val faceHeight = abs(mappedBottom - mappedTop)

        canvas.save()
        if (isFrontCamera) {
            canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }
        when (currentFilter) {
            "sunglasses" -> {
                if (leftEye != null && rightEye != null) {
                    val mappedLeftEyeX = leftEye.x * scaleX * flipX + translateX
                    val mappedRightEyeX = rightEye.x * scaleX * flipX + translateX
                    val mappedEyeY = (leftEye.y + rightEye.y) / 2f * scaleY
                    val anchorX = (mappedLeftEyeX + mappedRightEyeX) / 2f
                    val anchorY = mappedEyeY + (faceHeight * 0.05f)
                    val scale = faceWidth / bmp.width.toFloat() * 1.3f
                    canvas.translate(anchorX, anchorY)
                    canvas.scale(scale, scale)
                    canvas.drawBitmap(bmp, -bmp.width.toFloat() / 2f, -bmp.height.toFloat() / 2f, paint)
                }
            }
            "cat_ears" -> {
                val anchorX = (mappedLeft + mappedRight) / 2f
                val anchorY = mappedTop - (faceHeight * 0.2f)
                val scale = faceWidth / bmp.width.toFloat() * 1.5f
                canvas.translate(anchorX, anchorY)
                canvas.scale(scale, scale)
                canvas.drawBitmap(bmp, -bmp.width.toFloat() / 2f, -bmp.height.toFloat() / 2f, paint)
            }
            "hat" -> {
                if (noseBase != null) {
                    val anchorX = noseBase.x * scaleX * flipX + translateX
                    val anchorY = mappedTop - (faceHeight * 0.3f)
                    val scale = faceWidth / bmp.width.toFloat() * 1.4f
                    canvas.translate(anchorX, anchorY)
                    canvas.scale(scale, scale)
                    canvas.drawBitmap(bmp, -bmp.width.toFloat() / 2f, -bmp.height.toFloat() / 2f, paint)
                }
            }
        }
        canvas.restore()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawJob?.cancel()
    }
}
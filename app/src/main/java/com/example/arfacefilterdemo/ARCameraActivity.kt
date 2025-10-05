package com.example.arfacefilterdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arfacefilterdemo.databinding.ActivityArCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ARCameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var faceDetector: FaceDetector? = null
    private var isFrontCamera = true
    var currentFilter = "none"
    private val filterBitmaps = mutableMapOf<String, Int>()
    private var overlayView: FilterSurfaceView? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAG = "ARCameraActivity"
        fun newIntent(context: Context): Intent = Intent(context, ARCameraActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newFixedThreadPool(2)
        preloadFilters()
        initFaceDetector()
        overlayView = FilterSurfaceView(this)
        binding.overlayContainer.addView(overlayView)
        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        binding.captureButton.setOnClickListener { takePhoto() }
        binding.flipCameraButton.setOnClickListener { switchCamera() }
    }

    private fun preloadFilters() {
        filterBitmaps["sunglasses"] = R.drawable.sunglasses
        filterBitmaps["cat_ears"] = R.drawable.cat_ears
        filterBitmaps["hat"] = R.drawable.hat
    }

    private fun initFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .enableTracking()
            .build()
        faceDetector = FaceDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!overlayView?.isProcessingFrame()!!) {
                            processFrame(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }
            val cameraSelector = if (isFrontCamera)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        faceDetector?.process(image)
            ?.addOnSuccessListener { faces ->
                overlayView?.updateFaces(faces, currentFilter, filterBitmaps, isFrontCamera)
                binding.debugText.text = "Faces: ${faces.size}"
            }
            ?.addOnFailureListener { e -> Log.e(TAG, "Face detection failed", e) }
            ?.addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(externalCacheDir, "AR_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@ARCameraActivity, "Ảnh đã lưu!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun switchCamera() {
        isFrontCamera = !isFrontCamera
        startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    fun setFilter(filter: String) {
        currentFilter = filter
        overlayView?.updateFilter(filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        faceDetector?.close()
        cameraExecutor.shutdown()
    }
}
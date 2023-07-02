package com.androidcourse.hw7.ui.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.androidcourse.hw7.R
import com.androidcourse.hw7.databinding.ActivityTakePhotoBinding
import com.androidcourse.hw7.utils.MyApp
import java.util.concurrent.TimeUnit

class TakePhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTakePhotoBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraControl: CameraControl
    private val cameraSelectorDeque =
        ArrayDeque(listOf(CameraSelector.DEFAULT_BACK_CAMERA, CameraSelector.DEFAULT_FRONT_CAMERA))
    private val flashModeDeque = ArrayDeque(listOf(FLASH_MODE_AUTO, FLASH_MODE_OFF, FLASH_MODE_ON))
    private val flashMap = mapOf(
        FLASH_MODE_AUTO to R.drawable.baseline_flash_auto_24,
        FLASH_MODE_OFF to R.drawable.baseline_flash_off_24,
        FLASH_MODE_ON to R.drawable.baseline_flash_on_24
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setChangeFlashModeButtonOnClickListener()
        setChangeCameraButtonOnClickListener()
        startCamera()
        setFocusListener()
        setTakePhotoButtonListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFocusListener() {
        binding.previewView.setOnTouchListener { it, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                val pointFactory = SurfaceOrientedMeteringPointFactory(
                    it.width.toFloat(), it.height.toFloat()
                )
                val autoFocusPoint = pointFactory.createPoint(x, y)
                val focusMeteringAction =
                    FocusMeteringAction.Builder(autoFocusPoint, FocusMeteringAction.FLAG_AF)
                        .addPoint(autoFocusPoint, FocusMeteringAction.FLAG_AE)
                        .setAutoCancelDuration(5, TimeUnit.SECONDS)
                        .build()

                cameraControl.startFocusAndMetering(focusMeteringAction)
            }
            true
        }
    }

    private fun <T> update(deq: ArrayDeque<T>) {
        deq.addLast(deq.first())
        deq.removeFirst()
        startCamera()
    }

    private fun setChangeCameraButtonOnClickListener() {
        binding.changeCameraBtn.setOnClickListener {
            update(cameraSelectorDeque)
        }
    }

    private fun setChangeFlashModeButtonOnClickListener() {
        binding.changeFlashBtn.setOnClickListener {
            update(flashModeDeque)
            binding.changeFlashBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    flashMap[flashModeDeque.first()]!!
                )
            )
        }
    }

    private fun setTakePhotoButtonListener() {
        val outputOption = OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply { put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") })
            .build()
        binding.takePhotoBtn.setOnClickListener {
            imageCapture.takePicture(
                outputOption,
                ContextCompat.getMainExecutor(this@TakePhotoActivity),
                object : OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: OutputFileResults) {
                        val uri = outputFileResults.savedUri
                        if (uri != null) {
                            val returnIntent = Intent()
                            returnIntent.putExtra("result", uri)
                            setResult(RESULT_OK, returnIntent)
                        }
                        finish()

                    }

                    override fun onError(exception: ImageCaptureException) {
                        MyApp.instance.makeToast("Can't take photo")
                    }

                })
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
            imageCapture = Builder()
                .setFlashMode(flashModeDeque.first())
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(Size(640, 480))
                .build()
            val cameraSelector = cameraSelectorDeque.first()
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            cameraControl = camera.cameraControl
        }, ContextCompat.getMainExecutor(this))
    }
}
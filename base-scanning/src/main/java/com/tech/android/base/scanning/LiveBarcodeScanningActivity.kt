package com.tech.android.base.scanning

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.internal.Objects
import com.google.mlkit.vision.common.InputImage
import com.tech.android.base.scanning.barcodedetection.BarcodeDetector
import com.tech.android.base.scanning.barcodedetection.BarcodeProcessor
import com.tech.android.base.scanning.camera.CameraSource
import com.tech.android.base.scanning.camera.CameraSourcePreview
import com.tech.android.base.scanning.camera.GraphicOverlay
import com.tech.android.base.scanning.camera.WorkflowModel
import java.io.IOException

class LiveBarcodeScanningActivity : AppCompatActivity(), View.OnClickListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var flashButton: AppCompatImageView? = null
    private var workflowModel: WorkflowModel? = null
    private var currentWorkflowState: WorkflowModel.WorkflowState? = null
    private var inputBitmap: Bitmap? = null
    private var photoSelectResult: AppCompatImageView? = null
    private var detector: BarcodeDetector? = null
    private var loadingView: ProgressBar? = null
    private var _permissionCallback: ((Boolean?) -> Unit)? = null
    private val startForPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions.entries.none { !it.value }
            _permissionCallback?.invoke(isGranted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)

        preview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
            cameraSource = CameraSource(this)
        }

        flashButton = findViewById<AppCompatImageView>(R.id.scan_action_light).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
        }

        findViewById<View>(R.id.scan_action_back).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
        }

        findViewById<View>(R.id.scan_photo_select).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
        }

        photoSelectResult = findViewById(R.id.scan_photo_select_result)
        loadingView = findViewById(R.id.search_progress_bar)

        setUpWorkflowModel()
    }

    override fun onResume() {
        super.onResume()
        if (workflowModel?.isPhotoSelectMode != true) {
            workflowModel?.markCameraFrozen()
            currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
            cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
            workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
        }
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        cameraSource = null
        detector = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.scan_action_back -> onBackPressed()
            R.id.scan_action_light -> {
                flashButton?.let {
                    if (it.isSelected) {
                        it.isSelected = false
                        it.setImageResource(R.drawable.scan_action_light_off)
                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                    } else {
                        it.isSelected = true
                        it.setImageResource(R.drawable.scan_action_light_on)
                        cameraSource!!.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                    }
                }
            }
            R.id.scan_photo_select -> {
                workflowModel?.markPhotoSelectMode()
                Utils.openImagePicker(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Utils.REQUEST_CODE_PHOTO_LIBRARY && resultCode == Activity.RESULT_OK) {
            data?.data?.let(::detectBarcode)
        } else {
            workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun detectBarcode(imageUri: Uri) {
        workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.CONFIRMING)
        photoSelectResult?.setImageDrawable(null)
        try {
            inputBitmap = Utils.loadImage(
                this, imageUri,
                MAX_IMAGE_DIMENSION
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load file: $imageUri", e)
            workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
            return
        }
        photoSelectResult?.setImageBitmap(inputBitmap)

        if (detector == null) {
            detector = BarcodeDetector(workflowModel!!)
        }
        val image = InputImage.fromBitmap(inputBitmap!!, 0)
        detector?.process(image)
    }


    private fun startCameraPreview() {

        //request camera permission

        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start camera preview!", e)
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            flashButton?.isSelected = false
            preview?.stop()
        }
    }


    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProvider(this).get(WorkflowModel::class.java)

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")
            when (workflowState) {
                WorkflowModel.WorkflowState.DETECTING -> {
                    workflowModel?.markPhotoSelectFrozen()
                    loadingView?.visibility = View.GONE
                    photoSelectResult?.visibility = View.GONE
                    startCameraPreview()
                }
                WorkflowModel.WorkflowState.CONFIRMING -> {
                    loadingView?.visibility = View.VISIBLE
                    photoSelectResult?.visibility = View.VISIBLE
                    stopCameraPreview()
                }
                WorkflowModel.WorkflowState.SEARCHING -> {
                    stopCameraPreview()
                }
                WorkflowModel.WorkflowState.SEARCHED -> {
                    workflowModel?.markPhotoSelectFrozen()
                    loadingView?.visibility = View.GONE
                    stopCameraPreview()
                }
                else -> {
                    loadingView?.visibility = View.GONE
                    photoSelectResult?.visibility = View.GONE
                }
            }
        })

        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
            if (barcode != null) {
                setScanResult(barcode.rawValue)
            }
        })
    }

    private fun setScanResult(rawValue: String?) {
        if (TextUtils.isEmpty(rawValue)) return
        setResult(
            RESULT_OK,
            Intent().apply { putExtras(Bundle().apply { putString("result", rawValue) }) })
        finish()
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(
        permissionCallback: ((Boolean?) -> Unit)? = null,
        vararg permission: String,
    ) {
        this._permissionCallback = permissionCallback
        startForPermission.launch(arrayOf(*permission))
    }

    companion object {
        private const val TAG = "LiveBarcodeActivity"
        private const val MAX_IMAGE_DIMENSION = 1024
    }
}
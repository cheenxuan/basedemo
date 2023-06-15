package com.tech.android.base.scanning.barcodedetection

import android.animation.ValueAnimator
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tech.android.base.scanning.ScopedExecutor
import com.tech.android.base.scanning.camera.GraphicOverlay
import com.tech.android.base.scanning.camera.WorkflowModel

/**
 * @auther: xuan
 * @date  : 2023/6/14 .
 * <P>
 * Description:
 * <P>
 */
class BarcodeDetector(private val workflowModel: WorkflowModel) {

    private val scanner = BarcodeScanning.getClient()
    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    fun process(image: InputImage) {
        detectInImage(image)
            .addOnSuccessListener(executor) { results ->
                val containsBarCode = results.firstOrNull()
                if (containsBarCode == null) {
                    workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
                } else {
                    val loadingAnimator = createLoadingAnimator(containsBarCode)
                    loadingAnimator.start()
                    workflowModel.setWorkflowState(WorkflowModel.WorkflowState.SEARCHING)
                }
            }
            .addOnFailureListener(executor) { e ->
                workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
                Log.e(TAG, "Barcode detection failed!", e)
            }
    }

    fun detectInImage(image: InputImage): Task<List<Barcode>> = scanner.process(image)

    private fun createLoadingAnimator(
        barcode: Barcode,
    ): ValueAnimator {
        val endProgress = 1.1f
        return ValueAnimator.ofFloat(0f, endProgress).apply {
            duration = 1000
            addUpdateListener {
                if ((animatedValue as Float).compareTo(endProgress) >= 0) {
                    workflowModel.setWorkflowState(WorkflowModel.WorkflowState.SEARCHED)
                    workflowModel.detectedBarcode.setValue(barcode)
                }
            }
        }
    }

    companion object {
        const val TAG = "BarcodeDetector"
    }
}
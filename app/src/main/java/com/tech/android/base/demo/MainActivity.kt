package com.tech.android.base.demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.tech.android.base.scanning.LiveBarcodeScanningActivity
import com.tech.android.base.scanning.Utils
import com.tech.android.base.scanning.camera.WorkflowModel

class MainActivity : AppCompatActivity() {

    private var scanningResult: TextView? = null
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<View>(R.id.btn_scanning).setOnClickListener { 
            val intent = Intent(this,LiveBarcodeScanningActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE_BARCODE_SCANNING)
        }

        scanningResult = findViewById(R.id.scanning_result)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_BARCODE_SCANNING && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra("result")
            scanningResult?.text = result
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    companion object{
        const val REQUEST_CODE_BARCODE_SCANNING = 1
    }
}
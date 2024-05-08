package com.example.myapplication
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private val cameraPermissionRequestCode = 100
    private var capturedPhoto: File? = null // Variable to hold the captured photo temporarily

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasCameraPermission()) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.e(TAG, "Error creating the photo file: ${ex.message}")
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.example.myapplication.fileprovider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFileName = "JPEG_${timeStamp}_"
        val imageFile = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        capturedPhoto = imageFile // Assign the created image file to capturedPhoto
        return imageFile
    }


    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            cameraPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Handle permission denied scenario
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // Proceed to FunctionalitySelectionActivity with the photo path
                capturedPhoto?.let { photoFile ->
                    val intent = Intent(this, FunctionalitySelectionActivity::class.java)
                    intent.putExtra("photo_path", photoFile.absolutePath)
                    startActivity(intent)
                    finish() // Finish this activity
                }
            } else {
                Log.e(TAG, "Image capture failed with resultCode: $resultCode")
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val TAG = "CameraActivity"
    }
}

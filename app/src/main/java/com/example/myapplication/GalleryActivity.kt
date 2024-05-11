package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryActivity : AppCompatActivity() {

    private val galleryRequestCode: Int = 102
    private var selectedPhoto: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openGallery()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, galleryRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            // Handle the selected image
            val selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val imageFileName = "JPEG_${timeStamp}_"
                    val storageDir = externalCacheDir ?: filesDir
                    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
                    inputStream?.use { input ->
                        imageFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    selectedPhoto = imageFile
                    proceedToNextActivity()
                } catch (ex: IOException) {
                    Log.e(TAG, "Error copying selected image: ${ex.message}")
                }
            }
        } else {
            Log.e(TAG, "Gallery selection failed with resultCode: $resultCode")
        }
    }

    private fun proceedToNextActivity() {
        selectedPhoto?.let { photoFile ->
            val intent = Intent(this, FunctionalitySelectionActivity::class.java)
            intent.putExtra("photo_path", photoFile.absolutePath)
            startActivity(intent)
            finish() //
        }
    }

    companion object {
        private const val TAG = "GalleryActivity"
    }
}

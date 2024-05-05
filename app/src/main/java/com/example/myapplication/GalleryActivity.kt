package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

class GalleryActivity : AppCompatActivity() {

    private val galleryRequestCode: Int = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openGallery()
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivity(galleryIntent) // Open gallery without selecting image
        finish() // Finish this activity after opening the gallery
    }
}

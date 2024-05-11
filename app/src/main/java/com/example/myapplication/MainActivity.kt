package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cameraButton: Button = findViewById(R.id.button2)
        val galleryButton: Button = findViewById(R.id.button1)

        cameraButton.setOnClickListener {
            // Start CameraActivity to capture a photo
            startActivity(Intent(this, CameraActivity::class.java))
        }

        galleryButton.setOnClickListener {
            // Start GalleryActivity to view gallery
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

    // This method is called after the user takes a photo in CameraActivity
    private fun showFunctionalitySelection() {
        // Inflate the functionality selection layout
        setContentView(R.layout.choose_action)

    }
}

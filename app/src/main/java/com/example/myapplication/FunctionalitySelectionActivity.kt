package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class FunctionalitySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive the photo path from CameraActivity
        val photoPath = intent.getStringExtra("photo_path")

        // Open the choose_action layout
        setContentView(R.layout.choose_action)

        // Find the rotation button
        val rotationButton = findViewById<View>(R.id.Image_rotation)

        // Set OnClickListener for the rotation button
        rotationButton.setOnClickListener {
            // Start RotateActivity when the button is clicked
            val intent = Intent(this, RotateActivity::class.java)
            intent.putExtra("photo_path", photoPath) // Pass the photo path to RotateActivity
            startActivity(intent)
        }

        // Further actions based on the received photo path can be performed here
    }
}

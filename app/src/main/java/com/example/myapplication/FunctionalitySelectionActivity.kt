package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity



class FunctionalitySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive the photo path from CameraActivity
        val photoPath = intent.getStringExtra("photo_path")

        // Open the choose_action layout
        setContentView(R.layout.choose_action)

        // Further actions based on the received photo path can be performed here
    }
}

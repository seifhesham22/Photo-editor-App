package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class FunctionalitySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoPath = intent.getStringExtra("photo_path")

        setContentView(R.layout.choose_action)

        // Find the rotation button
        val rotationButton = findViewById<View>(R.id.Image_rotation)
        val filtersButton = findViewById<View>(R.id.colour_filter)
        val scalingButton = findViewById<View>(R.id.Image_scaling)
        val retouchingButton = findViewById<View>(R.id.Affinetransformations)


        rotationButton.setOnClickListener {

            val intent = Intent(this, RotateActivity::class.java)
            intent.putExtra("photo_path", photoPath) // Pass the photo path to RotateActivity
            startActivity(intent)
        }
        filtersButton.setOnClickListener {
            val intent = Intent(this, ColorFiltersActivity::class.java)
            intent.putExtra("photo_path", photoPath)
            startActivity(intent)
        }
        scalingButton.setOnClickListener {
            val intent = Intent(this, ImageScalingActivity::class.java)
            intent.putExtra("photo_path", photoPath)
            startActivity(intent)
        }
        retouchingButton.setOnClickListener {
            val intent = Intent(this, AffineTrans::class.java)
            intent.putExtra("photo_path" , photoPath)
            startActivity(intent)
        }


    }
}
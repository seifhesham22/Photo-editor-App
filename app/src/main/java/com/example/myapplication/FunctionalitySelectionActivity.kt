package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class FunctionalitySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_action)

        val photoPath = intent.getStringExtra("photo_path")

        // Find the buttons
        val rotationButton = findViewById<View>(R.id.Image_rotation)
        val filtersButton = findViewById<View>(R.id.colour_filter)
        val scalingButton = findViewById<View>(R.id.Image_scaling)
        val retouchingButton = findViewById<View>(R.id.Affinetransformations)
        val vectorButton = findViewById<View>(R.id.Vector_editor)
        val unsharpButton = findViewById<View>(R.id.Unsharp_masking)
        val backToMainButton = findViewById<View>(R.id.backtomain)

        // Set click listeners for the buttons
        rotationButton.setOnClickListener {
            val intent = Intent(this, RotateActivity::class.java)
            intent.putExtra("photo_path", photoPath)
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
            intent.putExtra("photo_path", photoPath)
            startActivity(intent)
        }

        vectorButton.setOnClickListener {
            val intent = Intent(this, VectorEditorActivity::class.java)
            intent.putExtra("photo_path", photoPath)
            startActivity(intent)
        }

        unsharpButton.setOnClickListener {
            val intent = Intent(this, UnsharpActivity::class.java)
            intent.putExtra("photo_path", photoPath)
            startActivity(intent)
        }


        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ImageScalingActivity : AppCompatActivity() {

    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_scaling)


        val photoPath = intent.getStringExtra("photo_path")


        originalBitmap = BitmapFactory.decodeFile(photoPath)


        val imageView = findViewById<ImageView>(R.id.scaleview)
        imageView.setImageBitmap(originalBitmap)


        findViewById<Button>(R.id.buttonScale).setOnClickListener {
            val scaleInput = findViewById<EditText>(R.id.scaleInput).text.toString().toFloatOrNull()
            if (scaleInput != null && scaleInput > 0) {
                scaleImage(scaleInput)
            } else {

            }
        }
    }

    private fun scaleImage(scaleRatio: Float) {
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        val newWidth = (originalWidth * scaleRatio).toInt()
        val newHeight = (originalHeight * scaleRatio).toInt()
        val scaleX = newWidth.toFloat() / originalWidth
        val scaleY = newHeight.toFloat() / originalHeight
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val offsetX = (newWidth - originalWidth * scaleX) / 2f
        val offsetY = (newHeight - originalHeight * scaleY) / 2f


        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val originalX = ((x - offsetX) / scaleX).toInt().coerceIn(0, originalWidth - 1)
                val originalY = ((y - offsetY) / scaleY).toInt().coerceIn(0, originalHeight - 1)
                val pixel = originalBitmap.getPixel(originalX, originalY)
                scaledBitmap.setPixel(x, y, pixel)
            }
        }


        val imageView = findViewById<ImageView>(R.id.scaleview)
        imageView.setImageBitmap(scaledBitmap)

        // Adjust ImageView size programmatically
        imageView.layoutParams.width = originalWidth
        imageView.layoutParams.height = originalHeight
    }}


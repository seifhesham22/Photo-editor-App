package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.RetouchingBinding
import java.io.IOException
import kotlin.math.pow
import kotlin.math.sqrt

class RetouchingActivity : AppCompatActivity() {

    private lateinit var binding: RetouchingBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var retouchedBitmap: Bitmap
    private var retouchingMode = false
    private var radius = 10f
    private val touchedPixels = mutableListOf<Pair<Float, Float>>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RetouchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve photo path from intent
        val photoPath = intent.getStringExtra("photo_path")

        // Load the original image
        originalBitmap = BitmapFactory.decodeFile(photoPath)
        retouchedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Display the original image
        binding.imageView.setImageBitmap(originalBitmap)

        // Set touch listener to handle retouching
        binding.imageView.setOnTouchListener { _, event ->
            if (retouchingMode && event != null) {
                handleRetouching(event)
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        // Button click listener to toggle retouching mode
        binding.retouchButton.setOnClickListener {
            retouchingMode = !retouchingMode
            if (retouchingMode) {
                binding.retouchButton.text = getString(R.string.finish_retouching)
            } else {
                binding.retouchButton.text = getString(R.string.start_retouching)
                binding.imageView.setImageBitmap(retouchedBitmap)
            }
        }

        // Button click listener to save the retouched image
        binding.saveButton.setOnClickListener {
            saveBitmapToGallery(retouchedBitmap)
        }
    }

    private fun handleRetouching(event: MotionEvent) {
        val imageView = binding.imageView
        val points = floatArrayOf(event.x, event.y)

        val matrix = Matrix()
        imageView.imageMatrix.invert(matrix)
        matrix.mapPoints(points)

        val touchX = points[0]
        val touchY = points[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                touchedPixels.add(Pair(touchX, touchY))
                retouchedBitmap = applyBlur(originalBitmap, touchedPixels, radius)
                binding.imageView.setImageBitmap(retouchedBitmap)
            }
        }
    }

    private fun applyBlur(bitmap: Bitmap, touchedPixels: List<Pair<Float, Float>>, radius: Float): Bitmap {
        val retouchedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        for (pixel in touchedPixels) {
            val x = pixel.first.toInt()
            val y = pixel.second.toInt()
            applyBoxBlur(retouchedBitmap, x, y, radius.toInt())
        }

        return retouchedBitmap
    }

    private fun applyBoxBlur(bitmap: Bitmap, centerX: Int, centerY: Int, radius: Int) {
        val width = bitmap.width
        val height = bitmap.height

        for (x in (centerX - radius).coerceAtLeast(0) until (centerX + radius).coerceAtMost(width)) {
            for (y in (centerY - radius).coerceAtLeast(0) until (centerY + radius).coerceAtMost(height)) {
                val distance = sqrt(((x - centerX).toDouble().pow(2.0) + (y - centerY).toDouble().pow(2.0)))
                if (distance <= radius) {
                    bitmap.setPixel(x, y, calculateAverageColor(bitmap, x, y, radius))
                }
            }
        }
    }

    private fun calculateAverageColor(bitmap: Bitmap, centerX: Int, centerY: Int, radius: Int): Int {
        var totalRed = 0
        var totalGreen = 0
        var totalBlue = 0
        var count = 0

        for (x in (centerX - radius).coerceAtLeast(0) until (centerX + radius).coerceAtMost(bitmap.width)) {
            for (y in (centerY - radius).coerceAtLeast(0) until (centerY + radius).coerceAtMost(bitmap.height)) {
                val distance = sqrt(((x - centerX).toDouble().pow(2.0) + (y - centerY).toDouble().pow(2.0)))
                if (distance <= radius) {
                    val pixelColor = bitmap.getPixel(x, y)
                    totalRed += Color.red(pixelColor)
                    totalGreen += Color.green(pixelColor)
                    totalBlue += Color.blue(pixelColor)
                    count++
                }
            }
        }

        val averageRed = totalRed / count
        val averageGreen = totalGreen / count
        val averageBlue = totalBlue / count

        return Color.rgb(averageRed, averageGreen, averageBlue)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Retouched_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RetouchedImages")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
                        Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show()
        }
    }
}


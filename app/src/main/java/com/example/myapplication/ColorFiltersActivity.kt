package com.example.myapplication

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class ColorFiltersActivity : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private lateinit var originalBitmap: Bitmap
    private var filteredBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filters)

        val photoPath = intent.getStringExtra("photo_path")

        // Load the photo into the ImageView
        photoImageView = findViewById(R.id.View2)
        photoPath?.let {
            originalBitmap = BitmapFactory.decodeFile(it)
            photoImageView.setImageBitmap(originalBitmap)
        }

        val grayscaleButton: Button = findViewById(R.id.grayscaleButton)
        grayscaleButton.setOnClickListener {
            applyGrayscaleFilter()
        }

        val sepiaButton: Button = findViewById(R.id.sepiaButton)
        sepiaButton.setOnClickListener {
            applySepiaFilter()
        }

        val invertButton: Button = findViewById(R.id.invertButton)
        invertButton.setOnClickListener {
            applyInvertFilter()
        }

        val brightnessButton: Button = findViewById(R.id.brightnessButton)
        brightnessButton.setOnClickListener {
            applyBrightnessFilter(30) // Adjust brightness level as needed
        }

        val contrastButton: Button = findViewById(R.id.contrastButton)
        contrastButton.setOnClickListener {
            applyContrastFilter(1.5f) // Adjust contrast level as needed
        }

        val saveButton: Button = findViewById(R.id.savebutton)
        saveButton.setOnClickListener {
            filteredBitmap?.let { bitmap ->
                saveBitmapToGallery(bitmap)
            } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyGrayscaleFilter() {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            val gray = (red + green + blue) / 3
            pixels[i] = Color.rgb(gray, gray, gray)
        }

        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        grayscaleBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        photoImageView.setImageBitmap(grayscaleBitmap)
        filteredBitmap = grayscaleBitmap
    }

    private fun applySepiaFilter() {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            var red = Color.red(pixel)
            var green = Color.green(pixel)
            var blue = Color.blue(pixel)

            val sepiaRed = (0.393 * red + 0.769 * green + 0.189 * blue).coerceAtMost(255.0)
            val sepiaGreen = (0.349 * red + 0.686 * green + 0.168 * blue).coerceAtMost(255.0)
            val sepiaBlue = (0.272 * red + 0.534 * green + 0.131 * blue).coerceAtMost(255.0)

            red = sepiaRed.toInt()
            green = sepiaGreen.toInt()
            blue = sepiaBlue.toInt()

            pixels[i] = Color.rgb(red, green, blue)
        }

        val sepiaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sepiaBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        photoImageView.setImageBitmap(sepiaBitmap)
        filteredBitmap = sepiaBitmap
    }

    private fun applyInvertFilter() {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = 255 - Color.red(pixel)
            val green = 255 - Color.green(pixel)
            val blue = 255 - Color.blue(pixel)

            pixels[i] = Color.rgb(red, green, blue)
        }

        val invertBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        invertBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        photoImageView.setImageBitmap(invertBitmap)
        filteredBitmap = invertBitmap
    }

    private fun applyBrightnessFilter(brightness: Int) {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (Color.red(pixel) + brightness).coerceIn(0, 255)
            val green = (Color.green(pixel) + brightness).coerceIn(0, 255)
            val blue = (Color.blue(pixel) + brightness).coerceIn(0, 255)
            pixels[i] = Color.rgb(red, green, blue)
        }

        val brightnessBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        brightnessBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        photoImageView.setImageBitmap(brightnessBitmap)
        filteredBitmap = brightnessBitmap
    }

    private fun applyContrastFilter(contrast: Float) {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val factor = (259 * (contrast + 255)) / (255 * (259 - contrast))

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (factor * (Color.red(pixel) - 128) + 128).coerceIn(0F, 255F)
            val green = (factor * (Color.green(pixel) - 128) + 128).coerceIn(0F, 255F)
            val blue = (factor * (Color.blue(pixel) - 128) + 128).coerceIn(0F, 255F)
            pixels[i] = Color.rgb(red, green, blue)
        }

        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        contrastBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        photoImageView.setImageBitmap(contrastBitmap)
        filteredBitmap = contrastBitmap
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Filtered_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FilteredImages")
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

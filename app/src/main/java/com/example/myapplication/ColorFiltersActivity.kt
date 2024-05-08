package com.example.myapplication
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class ColorFiltersActivity : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private lateinit var originalBitmap: Bitmap

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
    }}

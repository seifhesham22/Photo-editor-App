package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class UnsharpActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var photoPath: String
    private lateinit var ratioEditText: EditText
    private lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unsharp_mask)

        imageView = findViewById(R.id.imageView)
        ratioEditText = findViewById(R.id.ratioEditText)
        applyButton = findViewById(R.id.applyButton)

        // Get the photo path from the intent
        val extras = intent.extras
        if (extras != null) {
            photoPath = extras.getString("photo_path").toString()
            if (photoPath.isNotEmpty()) {
                val originalBitmap = BitmapFactory.decodeFile(photoPath)
                imageView.setImageBitmap(originalBitmap)

                applyButton.setOnClickListener {
                    val ratio = ratioEditText.text.toString().toFloatOrNull() ?: 0f
                    val sharpenedBitmap = applyUnsharpMask(originalBitmap, ratio)
                    imageView.setImageBitmap(sharpenedBitmap)
                }
            }
        }
    }

    private fun applyUnsharpMask(original: Bitmap, ratio: Float): Bitmap {
        // Convert to grayscale
        val grayscaleBitmap = toGrayscale(original)

        // Apply Gaussian blur
        val blurredBitmap = applyGaussianBlur(grayscaleBitmap)

        // Subtract blurred image from original and adjust ratio
        val sharpenedBitmap = subtractImages(grayscaleBitmap, blurredBitmap, ratio)

        return sharpenedBitmap
    }

    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width = bmpOriginal.width
        val height = bmpOriginal.height

        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = android.graphics.Canvas(bmpGrayscale)
        val paint = android.graphics.Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(0f)
        val f = android.graphics.ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        canvas.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun applyGaussianBlur(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        val radius = 1 // Gaussian blur radius

        // Gaussian kernel
        val kernel = arrayOf(
            floatArrayOf(1f, 2f, 1f),
            floatArrayOf(2f, 4f, 2f),
            floatArrayOf(1f, 2f, 1f)
        )

        for (x in radius until width - radius) {
            for (y in radius until height - radius) {
                var sumRed = 0f
                var sumGreen = 0f
                var sumBlue = 0f
                var sumAlpha = 0f

                for (i in -radius..radius) {
                    for (j in -radius..radius) {
                        val pixel = bitmap.getPixel(x + i, y + j)
                        val weight = kernel[i + radius][j + radius]
                        sumRed += Color.red(pixel) * weight
                        sumGreen += Color.green(pixel) * weight
                        sumBlue += Color.blue(pixel) * weight
                        sumAlpha += Color.alpha(pixel) * weight
                    }
                }

                val newPixel = Color.argb(
                    sumAlpha.toInt(),
                    sumRed.toInt() / 16,
                    sumGreen.toInt() / 16,
                    sumBlue.toInt() / 16
                )

                resultBitmap.setPixel(x, y, newPixel)
            }
        }

        return resultBitmap
    }
    private fun subtractImages(original: Bitmap, blurred: Bitmap, ratio: Float): Bitmap {
        val width = original.width
        val height = original.height
        val resultBitmap = Bitmap.createBitmap(width, height, original.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val originalPixel = original.getPixel(x, y)
                val blurredPixel = blurred.getPixel(x, y)

                val redDiff = Color.red(originalPixel) - Color.red(blurredPixel)
                val greenDiff = Color.green(originalPixel) - Color.green(blurredPixel)
                val blueDiff = Color.blue(originalPixel) - Color.blue(blurredPixel)

                val adjustedRed = Color.red(originalPixel) + (redDiff * ratio).toInt()
                val adjustedGreen = Color.green(originalPixel) + (greenDiff * ratio).toInt()
                val adjustedBlue = Color.blue(originalPixel) + (blueDiff * ratio).toInt()

                val finalPixel = Color.rgb(
                    clamp(adjustedRed),
                    clamp(adjustedGreen),
                    clamp(adjustedBlue)
                )

                resultBitmap.setPixel(x, y, finalPixel)
            }
        }

        return resultBitmap
    }

    private fun clamp(value: Int): Int {
        return if (value < 0) 0 else if (value > 255) 255 else value
    }

}
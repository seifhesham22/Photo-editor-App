package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class UnsharpActivity : AppCompatActivity() {

    private lateinit var originalBitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unsharp_mask)

        val photoPath = intent.getStringExtra("photo_path")
        originalBitmap = BitmapFactory.decodeFile(photoPath)

        imageView = findViewById(R.id.view20)
        imageView.setImageBitmap(originalBitmap)

        val applyUnsharpButton: Button = findViewById(R.id.button10)
        applyUnsharpButton.setOnClickListener {
            applyUnsharpMasking()
        }
    }

    private fun applyUnsharpMasking() {
        // Define unsharp masking parameters
        val radius = 5
        val amount = 0.5

        // Apply unsharp masking
        val sharpenedBitmap = unsharpMask(originalBitmap, radius, amount)

        // Display sharpened image
        imageView.setImageBitmap(sharpenedBitmap)
    }

    private fun unsharpMask(bitmap: Bitmap, radius: Int, amount: Double): Bitmap {
        // Convert bitmap to mutable bitmap for editing
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Apply unsharp masking algorithm
        val unsharpMaskedPixels = IntArray(mutableBitmap.width * mutableBitmap.height)
        mutableBitmap.getPixels(unsharpMaskedPixels, 0, mutableBitmap.width, 0, 0, mutableBitmap.width, mutableBitmap.height)

        for (i in unsharpMaskedPixels.indices) {
            val pixelValue = unsharpMaskedPixels[i]
            val r = Color.red(pixelValue)
            val g = Color.green(pixelValue)
            val b = Color.blue(pixelValue)

            val blurredPixelValue = applyGaussianBlur(unsharpMaskedPixels, i, mutableBitmap.width, mutableBitmap.height, radius)

            val newR = (r + (r - Color.red(blurredPixelValue)) * amount).toInt().coerceIn(0, 255)
            val newG = (g + (g - Color.green(blurredPixelValue)) * amount).toInt().coerceIn(0, 255)
            val newB = (b + (b - Color.blue(blurredPixelValue)) * amount).toInt().coerceIn(0, 255)

            unsharpMaskedPixels[i] = Color.rgb(newR, newG, newB)
        }

        mutableBitmap.setPixels(unsharpMaskedPixels, 0, mutableBitmap.width, 0, 0, mutableBitmap.width, mutableBitmap.height)

        return mutableBitmap
    }

    private fun applyGaussianBlur(pixels: IntArray, index: Int, width: Int, height: Int, radius: Int): Int {
        val r = radius.coerceAtLeast(1)
        val diameter = 2 * r + 1
        val matrix = DoubleArray(diameter)

        // Create Gaussian kernel
        val sigma = radius / 3.0
        var sigmaSquare = 2 * sigma * sigma
        var total = 0.0
        for (i in -r..r) {
            val distance = i * i
            matrix[i + r] = Math.exp(-distance / sigmaSquare) / (Math.PI * sigmaSquare)
            total += matrix[i + r]
        }

        // Normalize the kernel
        for (i in matrix.indices) {
            matrix[i] /= total
        }

        var red = 0
        var green = 0
        var blue = 0

        for (y in -r..r) {
            for (x in -r..r) {
                val pixelIndex = (index % width + x).coerceIn(0, width - 1) + (index / width + y).coerceIn(0, height - 1) * width
                val weight = matrix[y + r] * matrix[x + r]
                red += Color.red(pixels[pixelIndex]) * weight.toInt()
                green += Color.green(pixels[pixelIndex]) * weight.toInt()
                blue += Color.blue(pixels[pixelIndex]) * weight.toInt()
            }
        }

        return Color.rgb(red.coerceIn(0, 255), green.coerceIn(0, 255), blue.coerceIn(0, 255))
    }
}
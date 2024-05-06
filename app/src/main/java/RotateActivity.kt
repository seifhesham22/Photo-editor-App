package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RotateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotate)

        // Receive the photo path from FunctionalitySelectionActivity
        val photoPath = intent.getStringExtra("photo_path")

        // Load and display the original image
        val originalBitmap = BitmapFactory.decodeFile(photoPath)
        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageBitmap(originalBitmap)

        // Get the EditText for rotation degree input
        val rotationDegreeEditText: EditText = findViewById(R.id.editText_numbers)

        // Set a listener for when the rotation degree is submitted
        rotationDegreeEditText.setOnEditorActionListener { _, _, _ ->
            // Get the rotation degree from the EditText
            val rotationDegree = rotationDegreeEditText.text.toString().toFloatOrNull() ?: return@setOnEditorActionListener false

            // Rotate the original image
            val rotatedBitmap = rotateBitmap(originalBitmap, rotationDegree)

            // Display the rotated image
            imageView.setImageBitmap(rotatedBitmap)

            // Return true to indicate that the event has been consumed
            true
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        // Convert degrees to radians
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)

        // Get image dimensions
        val width = bitmap.width
        val height = bitmap.height

        // Calculate new image dimensions
        val newWidth = (abs(width * cos) + abs(height * sin)).toInt()
        val newHeight = (abs(width * sin) + abs(height * cos)).toInt()

        // Calculate pivot point
        val pivotX = width / 2f
        val pivotY = height / 2f

        // Calculate offsets to keep the rotated image within bounds
        val offsetX = (newWidth - width) / 2
        val offsetY = (newHeight - height) / 2

        // Create new bitmap for rotated image
        val rotatedBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)

        // Iterate through each pixel of the rotated image
        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                // Calculate coordinates in original image space
                val srcX = (x - pivotX - offsetX) * cos + (y - pivotY - offsetY) * sin + pivotX
                val srcY = (y - pivotY - offsetY) * cos - (x - pivotX - offsetX) * sin + pivotY

                // Perform bilinear interpolation to determine pixel color
                val pixel = bilinearInterpolation(bitmap, srcX.toFloat(), srcY.toFloat())

                // Set pixel color to rotated image
                rotatedBitmap.setPixel(x, y, pixel)
            }
        }

        return rotatedBitmap
    }


    private fun bilinearInterpolation(bitmap: Bitmap, x: Float, y: Float): Int {
        val xFloor = x.toInt()
        val yFloor = y.toInt()
        val xWeight = x - xFloor
        val yWeight = y - yFloor

        // Get the four surrounding pixels
        val pixel1 = getPixelSafe(bitmap, xFloor, yFloor)
        val pixel2 = getPixelSafe(bitmap, xFloor + 1, yFloor)
        val pixel3 = getPixelSafe(bitmap, xFloor, yFloor + 1)
        val pixel4 = getPixelSafe(bitmap, xFloor + 1, yFloor + 1)

        // Perform bilinear interpolation
        val red = bilinearInterpolate(pixel1 shr 16 and 0xFF, pixel2 shr 16 and 0xFF, pixel3 shr 16 and 0xFF, pixel4 shr 16 and 0xFF, xWeight, yWeight)
        val green = bilinearInterpolate(pixel1 shr 8 and 0xFF, pixel2 shr 8 and 0xFF, pixel3 shr 8 and 0xFF, pixel4 shr 8 and 0xFF, xWeight, yWeight)
        val blue = bilinearInterpolate(pixel1 and 0xFF, pixel2 and 0xFF, pixel3 and 0xFF, pixel4 and 0xFF, xWeight, yWeight)

        return 0xFF shl 24 or (red shl 16) or (green shl 8) or blue
    }

    private fun getPixelSafe(bitmap: Bitmap, x: Int, y: Int): Int {
        return if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
            bitmap.getPixel(x, y)
        } else {
            0 // Or any default color you prefer
        }
    }

    private fun bilinearInterpolate(p1: Int, p2: Int, p3: Int, p4: Int, xWeight: Float, yWeight: Float): Int {
        val value = (p1 * (1 - xWeight) * (1 - yWeight) +
                p2 * xWeight * (1 - yWeight) +
                p3 * yWeight * (1 - xWeight) +
                p4 * xWeight * yWeight).toInt()
        return value.coerceIn(0, 255)
    }
}

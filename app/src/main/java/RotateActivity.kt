package com.example.myapplication
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RotateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotate)


        val photoPath = intent.getStringExtra("photo_path")


        val originalBitmap = BitmapFactory.decodeFile(photoPath)
        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageBitmap(originalBitmap)


        val rotationDegreeEditText: EditText = findViewById(R.id.editText_numbers)


        rotationDegreeEditText.setOnEditorActionListener { _, _, _ ->

            val rotationDegree = rotationDegreeEditText.text.toString().toFloatOrNull() ?: return@setOnEditorActionListener false


            rotateBitmap(originalBitmap, rotationDegree) { rotatedBitmap ->
                // Display the rotated image
                imageView.setImageBitmap(rotatedBitmap)
            }


            true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun rotateBitmapAsync(bitmap: Bitmap, degrees: Float): Bitmap = withContext(Dispatchers.Default) {
        // Convert degrees to radians
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)

        val width = bitmap.width
        val height = bitmap.height

        // new image dimensions
        val newWidth = (abs(width * cos) + abs(height * sin)).toInt()
        val newHeight = (abs(width * sin) + abs(height * cos)).toInt()

        //pivot point
        val pivotX = width / 2f
        val pivotY = height / 2f

        // Calculate offsets for the rotated image tobe within bounds
        val offsetX = (newWidth - width) / 2
        val offsetY = (newHeight - height) / 2

        //new bitmap for the rotated image!
        val rotatedBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)

        // Create a list of jobs for each row of pixels "Parallel computing"
        val jobs = (0 until newHeight).map { y ->
            // Launch a coroutine for each row
            GlobalScope.async {
                // Iterate through each pixel of the rotated image row
                for (x in 0 until newWidth) {
                    // Calculate coordinates in original image space
                    val srcX = (x - pivotX - offsetX) * cos + (y - pivotY - offsetY) * sin + pivotX
                    val srcY = (y - pivotY - offsetY) * cos - (x - pivotX - offsetX) * sin + pivotY

                    // Get pixel color from the original image
                    val pixel = if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                        bitmap.getPixel(srcX.toInt(), srcY.toInt())
                    } else {
                        0
                    }

                    rotatedBitmap.setPixel(x, y, pixel)
                }
            }
        }

        jobs.forEach { it.await() }


        rotatedBitmap
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float, callback: (Bitmap) -> Unit) {
        // Launch coroutine to rotate the bitmap
        GlobalScope.launch(Dispatchers.Main) {
            val rotatedBitmap = rotateBitmapAsync(bitmap, degrees)
            // Execute the callback with the rotated bitmap on the main thread
            callback(rotatedBitmap)
        }
    }
}

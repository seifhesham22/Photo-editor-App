package com.example.myapplication

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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

    private lateinit var originalBitmap: Bitmap
    private var rotatedBitmap: Bitmap? = null
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotate)

        val photoPath = intent.getStringExtra("photo_path")
        originalBitmap = BitmapFactory.decodeFile(photoPath)
        imageView = findViewById(R.id.imageView)
        imageView.setImageBitmap(originalBitmap)

        val rotationDegreeEditText: EditText = findViewById(R.id.editText_numbers)
        val buttonRotate: Button = findViewById(R.id.button_rotate)
        val buttonSave: Button = findViewById(R.id.button_save)

        buttonRotate.setOnClickListener {
            val rotationDegree = rotationDegreeEditText.text.toString().toFloatOrNull() ?: return@setOnClickListener
            rotateBitmap(originalBitmap, rotationDegree) { bitmap ->
                rotatedBitmap = bitmap
                imageView.setImageBitmap(bitmap)
            }
        }

        buttonSave.setOnClickListener {
            rotatedBitmap?.let { bitmap ->
                saveBitmapToGallery(bitmap)
            } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun rotateBitmapAsync(bitmap: Bitmap, degrees: Float): Bitmap = withContext(Dispatchers.Default) {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)

        val width = bitmap.width
        val height = bitmap.height

        val newWidth = (abs(width * cos) + abs(height * sin)).toInt()
        val newHeight = (abs(width * sin) + abs(height * cos)).toInt()

        val pivotX = width / 2f
        val pivotY = height / 2f

        val offsetX = (newWidth - width) / 2
        val offsetY = (newHeight - height) / 2

        val rotatedBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)

        val jobs = (0 until newHeight).map { y ->
            GlobalScope.async {
                for (x in 0 until newWidth) {
                    val srcX = (x - pivotX - offsetX) * cos + (y - pivotY - offsetY) * sin + pivotX
                    val srcY = (y - pivotY - offsetY) * cos - (x - pivotX - offsetX) * sin + pivotY

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
        GlobalScope.launch(Dispatchers.Main) {
            val rotatedBitmap = rotateBitmapAsync(bitmap, degrees)
            callback(rotatedBitmap)
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Rotated_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RotatedImages")
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

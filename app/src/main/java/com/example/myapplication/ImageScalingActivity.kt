package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ImageScalingActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var srcBitmap: Bitmap
    private var resizedBitmap: Bitmap? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_scaling)

        imgView = findViewById(R.id.photoImageView)
        val scaleXEditText: EditText = findViewById(R.id.resizeXScaleEditText)
        val scaleYEditText: EditText = findViewById(R.id.resizeYScaleEditText)
        val resizeBtn: Button = findViewById(R.id.resizeButton)
        val saveBtn: Button = findViewById(R.id.save)

        val imgPath = intent.getStringExtra("photo_path")

        if (imgPath != null) {
            srcBitmap = BitmapFactory.decodeFile(imgPath)
            imgView.setImageBitmap(srcBitmap)

            resizeBtn.setOnClickListener {
                val scaleX = scaleXEditText.text.toString().toDoubleOrNull() ?: 1.0
                val scaleY = scaleYEditText.text.toString().toDoubleOrNull() ?: 1.0

                resizedBitmap = resize(srcBitmap, scaleX, scaleY)
                imgView.setImageBitmap(resizedBitmap)
            }

            saveBtn.setOnClickListener {
                resizedBitmap?.let { bitmap ->
                    saveBitmapToGallery(bitmap)
                } ?: Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resize(srcBitmap: Bitmap, scaleX: Double, scaleY: Double): Bitmap {
        val newWidth: Int = (srcBitmap.width * scaleX).toInt()
        val newHeight = (srcBitmap.height * scaleY).toInt()
        return if (scaleX + scaleY >= 2.0) {
            bilinearInterpolation(srcBitmap, scaleX, scaleY, newWidth, newHeight)
        } else {
            trilinearInterpolation(srcBitmap, scaleX, scaleY, newWidth, newHeight)
        }
    }

    private fun bilinearInterpolation(
        srcBitmap: Bitmap,
        scaleX: Double,
        scaleY: Double,
        newWidth: Int,
        newHeight: Int
    ): Bitmap {
        val resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val originalX = x / scaleX
                val originalY = y / scaleY
                val x0 = originalX.toInt().coerceIn(0, srcBitmap.width - 1)
                val y0 = originalY.toInt().coerceIn(0, srcBitmap.height - 1)
                val x1 = (originalX + 1).toInt().coerceIn(0, srcBitmap.width - 1)
                val y1 = (originalY + 1).toInt().coerceIn(0, srcBitmap.height - 1)
                val dx = originalX - x0
                val dy = originalY - y0

                val px1 = srcBitmap.getPixel(x0, y0)
                val px2 = srcBitmap.getPixel(x0, y1)
                val px3 = srcBitmap.getPixel(x1, y0)
                val px4 = srcBitmap.getPixel(x1, y1)

                val newC1 = calculateMiddleInterpolation(px1, px2, dx)
                val newC2 = calculateMiddleInterpolation(px3, px4, dx)

                resizedBitmap.setPixel(x, y, calculateMiddleInterpolation(newC1, newC2, dy))
            }
        }
        return resizedBitmap
    }

    private fun trilinearInterpolation(
        srcBitmap: Bitmap,
        scaleX: Double,
        scaleY: Double,
        newWidth: Int,
        newHeight: Int
    ): Bitmap {
        val resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val originalX = x / scaleX
                val originalY = y / scaleY
                val x0 = originalX.toInt().coerceIn(0, srcBitmap.width - 1)
                val y0 = originalY.toInt().coerceIn(0, srcBitmap.height - 1)
                val x1 = (originalX + 1).toInt().coerceIn(0, srcBitmap.width - 1)
                val y1 = (originalY + 1).toInt().coerceIn(0, srcBitmap.height - 1)
                val dx = originalX - x0
                val dy = originalY - y0

                val px1 = srcBitmap.getPixel(x0, y0)
                val px2 = srcBitmap.getPixel(x0, y1)
                val px3 = srcBitmap.getPixel(x1, y0)
                val px4 = srcBitmap.getPixel(x1, y1)

                val newC1 = calculateMiddleInterpolation(px1, px2, dx)
                val newC2 = calculateMiddleInterpolation(px3, px4, dx)
                val newC3 = calculateMiddleInterpolation(newC1, newC2, dy)

                resizedBitmap.setPixel(x, y, calculateMiddleInterpolation(newC3, newC3, 0.5))
            }
        }

        return resizedBitmap
    }

    private fun calculateMiddleInterpolation(px1: Int, px2: Int, coef: Double): Int {
        val newR = Color.red(px1) * (1 - coef) + Color.red(px2) * coef
        val newG = Color.green(px1) * (1 - coef) + Color.green(px2) * coef
        val newB = Color.blue(px1) * (1 - coef) + Color.blue(px2) * coef
        return (255 shl 24) or (newR.toInt() shl 16) or (newG.toInt() shl 8) or newB.toInt()
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Scaled_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ScaledImages")
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

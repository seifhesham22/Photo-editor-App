package com.example.myapplication

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AffineTrans : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private lateinit var resizeXScaleEditText: EditText
    private lateinit var resizeYScaleEditText: EditText
    private lateinit var resizeButton: Button

    private lateinit var originalPoints: MutableList<PointF>
    private lateinit var destinationPoints: MutableList<PointF>
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.affine)

        val photoPath = intent.getStringExtra("photo_path") ?: return
        photoImageView = findViewById(R.id.photoImageView)
        resizeXScaleEditText = findViewById(R.id.resizeXScaleEditText)
        resizeYScaleEditText = findViewById(R.id.resizeYScaleEditText)
        resizeButton = findViewById(R.id.resizeButton)

        bitmap = BitmapFactory.decodeFile(photoPath)
        photoImageView.setImageBitmap(bitmap)

        originalPoints = mutableListOf()
        destinationPoints = mutableListOf()

        photoImageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                if (originalPoints.size < 3) {
                    originalPoints.add(PointF(event.x, event.y))
                    Toast.makeText(this, "Original Point ${originalPoints.size} set", Toast.LENGTH_SHORT).show()
                } else if (destinationPoints.size < 3) {
                    destinationPoints.add(PointF(event.x, event.y))
                    Toast.makeText(this, "Destination Point ${destinationPoints.size} set", Toast.LENGTH_SHORT).show()
                }
            }
            v.performClick()
            true
        }

        resizeButton.setOnClickListener {
            resizeImage()
        }
    }

    fun onApplyButtonClick(view: View) {
        if (originalPoints.size == 3 && destinationPoints.size == 3) {
            applyAffineTransformation(bitmap)
        } else {
            Toast.makeText(this, "Please select three points on both images", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var transformedBitmap: Bitmap

    private fun applyAffineTransformation(bitmap: Bitmap) {
        val matrix = calculateAffineTransformMatrix(originalPoints, destinationPoints)

        transformedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(transformedBitmap)
        val paint = Paint().apply {
            isFilterBitmap = true
        }

        if (!matrix.isIdentity) {
            canvas.drawBitmap(bitmap, matrix, paint)
            photoImageView.setImageBitmap(transformedBitmap)
            originalPoints.clear()
            destinationPoints.clear()
        } else {
            Toast.makeText(this, "Transformation failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun calculateAffineTransformMatrix(originalPoints: List<PointF>, destinationPoints: List<PointF>): Matrix {
        val src = floatArrayOf(
            originalPoints[0].x, originalPoints[0].y,
            originalPoints[1].x, originalPoints[1].y,
            originalPoints[2].x, originalPoints[2].y
        )
        val dst = floatArrayOf(
            destinationPoints[0].x, destinationPoints[0].y,
            destinationPoints[1].x, destinationPoints[1].y,
            destinationPoints[2].x, destinationPoints[2].y
        )
        return Matrix().apply {
            if (!setPolyToPoly(src, 0, dst, 0, 3)) {
                reset()
            }
        }
    }

    private fun resizeImage() {
        val xScale = resizeXScaleEditText.text.toString().toFloatOrNull() ?: 1.0f
        val yScale = resizeYScaleEditText.text.toString().toFloatOrNull() ?: 1.0f

        if (xScale > 0 && yScale > 0) {
            val matrix = Matrix()
            matrix.postScale(xScale, yScale)

            val resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            photoImageView.setImageBitmap(resizedBitmap)
        } else {
            Toast.makeText(this, "Invalid scale values. Please enter valid numbers.", Toast.LENGTH_SHORT).show()
        }
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

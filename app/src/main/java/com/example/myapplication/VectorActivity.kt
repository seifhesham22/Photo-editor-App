package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class VectorEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vector)

        val photoPath = intent.getStringExtra("photo_path")

        val drawingView = findViewById<DrawingView>(R.id.drawingView)
        drawingView.setPhotoBitmap(photoPath)

        val interpolationButton = findViewById<Button>(R.id.interpolationButton)
        interpolationButton.setOnClickListener {
            drawingView.toggleInterpolation()
        }
    }

    // Custom View for drawing broken lines and splines
    class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
        private val pointPaint = Paint().apply {
            color = Color.RED // Color for the touch points
            style = Paint.Style.FILL // Filled circle
        }
        private val pathPaint = Paint().apply {
            color = Color.BLACK // Color for lines
            strokeWidth = 5f
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        private val touchPoints = mutableListOf<PointF>()
        private var isInterpolationEnabled = false
        private var photoBitmap: Bitmap? = null

        fun setPhotoBitmap(photoPath: String?) {
            photoBitmap = BitmapFactory.decodeFile(photoPath)
            invalidate() // Refresh the view when the photo bitmap is set
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw the background photo
            photoBitmap?.let {
                // Draw the image at the top
                val destRect = Rect(0, 0, canvas.width, it.height * canvas.width / it.width)
                canvas.drawBitmap(it, null, destRect, null)
            }

            // Draw the touch points
            for (point in touchPoints) {
                canvas.drawCircle(point.x, point.y, 10f, pointPaint) // Draw circles as touch points
            }

            // Draw the broken lines or splines based on touch points
            if (isInterpolationEnabled) {
                drawSpline(canvas)
            } else {
                drawBrokenLines(canvas)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val touchX = event.x
                    val touchY = event.y
                    touchPoints.add(PointF(touchX, touchY))
                    invalidate() // Redraw the view
                }
            }
            return true
        }

        private fun drawBrokenLines(canvas: Canvas) {
            for (i in 1 until touchPoints.size) {
                val startX = touchPoints[i - 1].x
                val startY = touchPoints[i - 1].y
                val endX = touchPoints[i].x
                val endY = touchPoints[i].y
                canvas.drawLine(startX, startY, endX, endY, pathPaint)
            }
        }

        private fun drawSpline(canvas: Canvas) {
            if (touchPoints.size >= 4) {
                val splinePoints = CatmullRomSpline.calculateCurve(touchPoints.toTypedArray(), 0.5f, false)
                for (i in 1 until splinePoints.size) {
                    val startX = splinePoints[i - 1].x
                    val startY = splinePoints[i - 1].y
                    val endX = splinePoints[i].x
                    val endY = splinePoints[i].y
                    canvas.drawLine(startX, startY, endX, endY, pathPaint)
                }
            }
        }

        fun toggleInterpolation() {
            isInterpolationEnabled = !isInterpolationEnabled
            invalidate() // Redraw the view
        }
    }
}

object CatmullRomSpline {
    fun calculateCurve(controlPoints: Array<PointF>, alpha: Float, isClosed: Boolean): List<PointF> {
        val result = mutableListOf<PointF>()

        val n = controlPoints.size
        if (n < 4) return emptyList() // Cannot create a spline with less than 4 points

        val increment = if (isClosed) 1 else 0
        for (i in 0 until n - 3 + increment) {
            for (t in 0..100) {
                val t0 = t.toFloat() / 100
                val t1 = t0 * t0
                val t2 = t1 * t0

                val h00 = 2 * t2 - 3 * t1 + 1
                val h10 = t2 - 2 * t1 + t0
                val h01 = -2 * t2 + 3 * t1
                val h11 = t2 - t1

                val p0 = controlPoints[i]
                val p1 = controlPoints[i + 1]
                val p2 = controlPoints[i + 2]
                val p3 = controlPoints[i + 3]

                val px = 0.5f * ((2 * p1.x) + (-p0.x + p2.x) * t0 + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t1 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t2)
                val py = 0.5f * ((2 * p1.y) + (-p0.y + p2.y) * t0 + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t1 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t2)

                result.add(PointF(px, py))
            }
        }

        return result
    }
}

package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
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

    class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
        private val pointPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        private val pathPaint = Paint().apply {
            color = Color.BLACK
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
            invalidate()
        }

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            photoBitmap?.let {
                val destRect = Rect(0, 0, width, it.height * width / it.width)
                canvas.drawBitmap(it, null, destRect, null)
            }

            // Draw all the touch points as circles
            for (point in touchPoints) {
                canvas.drawCircle(point.x, point.y, 10f, pointPaint)
            }

            // Draw either the spline or broken lines based on the flag
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
            val path = Path()
            if (touchPoints.size >= 2) {
                val splinePoints = if (touchPoints.size == 2) {
                    listOf(touchPoints[0], touchPoints[1])
                } else {
                    CatmullRomSpline.calculateCurve(touchPoints.toTypedArray(), 0.5f, false)
                }

                if (splinePoints.isNotEmpty()) {
                    path.moveTo(splinePoints[0].x, splinePoints[0].y)
                    for (i in 1 until splinePoints.size) {
                        path.lineTo(splinePoints[i].x, splinePoints[i].y)
                    }
                    canvas.drawPath(path, pathPaint)
                }
            }
        }

        fun toggleInterpolation() {
            isInterpolationEnabled = !isInterpolationEnabled
            invalidate()
        }
    }
}

object CatmullRomSpline {
    fun calculateCurve(controlPoints: Array<PointF>, alpha: Float, isClosed: Boolean): List<PointF> {
        val result = mutableListOf<PointF>()
        val n = controlPoints.size

        if (n < 2) return emptyList() // Less than 2 points can't form a spline

        val increment = if (isClosed) 1 else 0
        for (i in 0 until n - 1 + increment) {
            val p0 = controlPoints[if (i == 0) n - 1 else (i - 1) % n]
            val p1 = controlPoints[i % n]
            val p2 = controlPoints[(i + 1) % n]
            val p3 = controlPoints[(i + 2) % n]

            for (t in 0..100) {
                val t0 = t / 100f
                val t1 = t0 * t0
                val t2 = t1 * t0

                val h00 = 2 * t2 - 3 * t1 + 1
                val h10 = t2 - 2 * t1 + t0
                val h01 = -2 * t2 + 3 * t1
                val h11 = t2 - t1

                val px = 0.5f * ((2 * p1.x) + (-p0.x + p2.x) * t0 + (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * t1 + (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * t2)
                val py = 0.5f * ((2 * p1.y) + (-p0.y + p2.y) * t0 + (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * t1 + (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * t2)

                result.add(PointF(px, py))
            }
        }

        return result
    }
} // m7





package com.example.memoryvisualizer.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import com.example.memoryvisualizer.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MemoryCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var renderBlocks: List<RenderBlockMapper.RenderBlock> = emptyList()
    private var totalSize: Int = 1

    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.scandi_border)
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.scandi_primary_600)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.scandi_text_high)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
    }

    // Interaction state
    private var selectedBlockId: String? = null
    private var viewScaleY: Float = 1f
    private var viewTranslateY: Float = 0f

    // Touch handling improvements for mobile
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialY = 0f
    private var isDragging = false
    private var isScaling = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScale = viewScaleY
            val newScale = (oldScale * detector.scaleFactor).coerceIn(0.5f, 5f)
            if (newScale != oldScale) {
                val focusY = detector.focusY
                val contentY = (focusY - viewTranslateY) / oldScale
                viewScaleY = newScale
                viewTranslateY = focusY - newScale * contentY
                clampTranslate()
                invalidate()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (!isDragging && !isScaling) {
                val clicked = findBlockAtContentY(toContentY(e.y))
                if (clicked != null) {
                    selectBlockById(clicked.id)
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    // announce click for accessibility
                    performClick()
                    onBlockClick?.invoke(clicked)
                    return true
                }
            }
            return super.onSingleTapUp(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (!isDragging && !isScaling) {
                val b = findBlockAtContentY(toContentY(e.y))
                if (b != null) {
                    selectBlockById(b.id)
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onBlockLongPress?.invoke(b)
                }
            }
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (!isScaling && abs(distanceY) > abs(distanceX)) {
                isDragging = true
                parent?.requestDisallowInterceptTouchEvent(true)
                viewTranslateY -= distanceY
                clampTranslate()
                invalidate()
                return true
            }
            return false
        }
    })

    var onBlockClick: ((RenderBlockMapper.RenderBlock) -> Unit)? = null
    var onBlockLongPress: ((RenderBlockMapper.RenderBlock) -> Unit)? = null

    fun submit(blocks: List<RenderBlockMapper.RenderBlock>) {
        renderBlocks = blocks
        totalSize = max(1, blocks.maxOfOrNull { it.start + it.size } ?: 1)
        contentDescription = context.getString(R.string.cd_memory_canvas, blocks.size)
        selectedBlockId = null
        // reset viewport on new data
        viewScaleY = 1f
        viewTranslateY = 0f
        invalidate()
    }

    fun selectBlockById(id: String?) {
        selectedBlockId = id
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (140 * resources.displayMetrics.density).toInt()
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (renderBlocks.isEmpty()) return
        val h = height.toFloat()
        val w = width.toFloat()
        val scale = h / totalSize
        val minLabelPx = 16f * resources.displayMetrics.density

        canvas.save()
        canvas.translate(0f, viewTranslateY)
        canvas.scale(1f, viewScaleY, 0f, 0f)

        renderBlocks.forEach { b ->
            val top = h - (b.start + b.size) * scale
            val bottom = h - b.start * scale
            val rect = RectF(0f, top, w, bottom)
            blockPaint.color = b.color
            canvas.drawRect(rect, blockPaint)
            canvas.drawRect(rect, borderPaint)
            val blockHeight = bottom - top
            if (!b.isFree && blockHeight >= minLabelPx) {
                val label = "${b.processId}(${b.size})"
                canvas.drawText(label, 8f, bottom - 8f, textPaint)
            } else if (b.isFree && blockHeight >= minLabelPx) {
                canvas.drawText("FREE ${b.size}", 8f, bottom - 8f, textPaint)
            }
            if (b.id == selectedBlockId) {
                canvas.drawRect(rect, highlightPaint)
            }
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (renderBlocks.isEmpty()) return super.onTouchEvent(event)

        var handled = scaleDetector.onTouchEvent(event)
        if (!scaleDetector.isInProgress) {
            handled = gestureDetector.onTouchEvent(event) || handled
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling && abs(event.y - initialY) > touchSlop) {
                    isDragging = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                isDragging = false
                isScaling = false
            }
        }

        return handled || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun clampTranslate() {
        val h = height.toFloat()
        val scaledHeight = h * viewScaleY
        val maxTranslate = if (scaledHeight > h) scaledHeight - h else 0f
        val minTranslate = 0f
        viewTranslateY = viewTranslateY.coerceIn(-maxTranslate, minTranslate)
    }

    private fun toContentY(y: Float): Float {
        return (y - viewTranslateY) / max(0.0001f, viewScaleY)
    }

    private fun findBlockAtContentY(contentY: Float): RenderBlockMapper.RenderBlock? {
        val h = height.toFloat()
        val scale = h / totalSize
        return renderBlocks.firstOrNull { b ->
            val top = h - (b.start + b.size) * scale
            val bottom = h - b.start * scale
            contentY in top..bottom
        }
    }
}

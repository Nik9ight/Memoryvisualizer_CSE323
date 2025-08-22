package com.example.memoryvisualizer.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.memoryvisualizer.R
import kotlin.math.max

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
        color = 0xFF444444.toInt()
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF111111.toInt()
        textSize = 12f * resources.displayMetrics.scaledDensity
    }

    fun submit(blocks: List<RenderBlockMapper.RenderBlock>) {
        renderBlocks = blocks
        totalSize = max(1, blocks.maxOfOrNull { it.start + it.size } ?: 1)
        contentDescription = context.getString(R.string.cd_memory_canvas, blocks.size)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (120 * resources.displayMetrics.density).toInt()
        val desiredHeight = MeasureSpec.getSize(heightMeasureSpec).takeIf { it > 0 } ?: (400 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (renderBlocks.isEmpty()) return
        
        val h = height.toFloat()
        val w = width.toFloat()
        val scale = w / totalSize

        val rect = RectF()
        for (block in renderBlocks) {
            val left = block.start * scale
            val right = (block.start + block.size) * scale

            rect.set(left, 0f, right, h)
            blockPaint.color = when {
                block.isProcess -> 0xFF2196F3.toInt() // Blue for processes
                block.isFree -> 0xFFE0E0E0.toInt() // Light gray for free blocks
                else -> 0xFFF44336.toInt() // Red for other blocks
            }
            canvas.drawRect(rect, blockPaint)
            canvas.drawRect(rect, borderPaint)

            if (block.processId != null) {
                val text = block.processId
                val textWidth = textPaint.measureText(text)
                val x = left + (right - left - textWidth) / 2
                val y = h / 2 + textPaint.textSize / 3
                canvas.drawText(text, x, y, textPaint)
            }
        }
        val scale = h / totalSize
        val minLabelPx = 14f * resources.displayMetrics.density

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
                canvas.drawText(label, 4f, bottom - 4f, textPaint)
            } else if (b.isFree && blockHeight >= minLabelPx) {
                canvas.drawText("FREE ${b.size}", 4f, bottom - 4f, textPaint)
            }
        }
    }
}

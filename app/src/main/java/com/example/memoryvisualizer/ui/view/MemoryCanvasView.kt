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

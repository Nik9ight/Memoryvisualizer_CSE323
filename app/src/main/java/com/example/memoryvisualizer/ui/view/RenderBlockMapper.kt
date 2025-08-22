package com.example.memoryvisualizer.ui.view

import com.example.memoryvisualizer.ui.util.ColorPalette

/** Maps stub blocks to render model used by MemoryCanvasView */
object RenderBlockMapper {
    data class RenderBlock(
        val id: String,
        val start: Int,
        val size: Int,
        val isFree: Boolean,
        val processId: String?,
        val internalFrag: Int,
        val color: Int
    )

    fun map(blocks: List<Int>): List<RenderBlock> =
        blocks.map { b ->
            RenderBlock(
                id = b.id,
                start = b.start,
                size = b.size,
                isFree = b.isFree,
                processId = b.processId,
                internalFrag = b.internalFrag,
                color = if (b.isFree) 0xFFE0E0E0.toInt() else ColorPalette.colorForProcess(b.processId)
            )
        }
}

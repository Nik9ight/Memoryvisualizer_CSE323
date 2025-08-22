package com.example.memoryvisualizer.ui.view

data class RenderBlock(
    val size: Int,
    val isProcess: Boolean,
    val isFree: Boolean = !isProcess,
    val processId: String? = null,
    val start: Int = 0
)

object RenderBlockMapper {
    fun map(blocks: List<Int>, allocations: List<Int> = emptyList()): List<RenderBlock> {
        val allBlocks = mutableListOf<RenderBlock>()
        var currentPosition = 0
        
        blocks.forEachIndexed { index, size ->
            allBlocks.add(RenderBlock(
                size = size,
                isProcess = index < allocations.size,
                processId = if (index < allocations.size) "P$index" else null,
                start = currentPosition
            ))
            currentPosition += size
        }
        
        return allBlocks
    }
}

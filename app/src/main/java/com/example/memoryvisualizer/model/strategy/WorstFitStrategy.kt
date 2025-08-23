package com.example.memoryvisualizer.model.strategy

import com.example.memoryvisualizer.model.MemoryBlock

/**
 * Worst Fit Strategy: Allocates the process to the largest available block,
 * maximizing the remaining fragment size for future allocations.
 */
class WorstFitStrategy : AllocationStrategy {
    
    override fun chooseBlock(blocks: List<MemoryBlock>, processSize: Int): Int {
        var worstFitIndex = -1
        var worstFitSize = -1
        
        blocks.forEachIndexed { index, block ->
            // Only consider free blocks that are large enough
            if (block.isFree && block.size >= processSize) {
                // If this block is larger than current worst fit, update worst fit
                if (block.size > worstFitSize) {
                    worstFitSize = block.size
                    worstFitIndex = index
                }
                // Tie-breaker: when sizes are equal, choose the block with lower address
                else if (block.size == worstFitSize && block.start < blocks[worstFitIndex].start) {
                    worstFitIndex = index
                }
            }
        }
        
        return worstFitIndex  // Returns -1 if no suitable block found
    }
    
    override val name: String = "Worst Fit"
}
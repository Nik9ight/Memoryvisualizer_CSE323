package com.example.memoryvisualizer.model.strategy

import com.example.memoryvisualizer.model.MemoryBlock

/**
 * Best Fit Strategy: Allocates the process to the smallest block that can fit it,
 * minimizing the wasted space.
 */
class BestFitStrategy : AllocationStrategy {
    
    override fun chooseBlock(blocks: List<MemoryBlock>, processSize: Int): Int {
        var bestFitIndex = -1
        var bestFitSize = Int.MAX_VALUE
        
        blocks.forEachIndexed { index, block ->
            // Only consider free blocks that are large enough
            if (block.isFree && block.size >= processSize) {
                // If this block is smaller than current best fit, update best fit
                if (block.size < bestFitSize) {
                    bestFitSize = block.size
                    bestFitIndex = index
                } 
                // Tie-breaker: when sizes are equal, choose the block with lower address
                else if (block.size == bestFitSize && block.start < blocks[bestFitIndex].start) {
                    bestFitIndex = index
                }
            }
        }
        
        return bestFitIndex  // Returns -1 if no suitable block found
    }
    
    override val name: String = "Best Fit"
}
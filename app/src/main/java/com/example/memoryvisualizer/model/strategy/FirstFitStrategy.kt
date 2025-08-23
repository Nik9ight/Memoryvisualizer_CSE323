package com.example.memoryvisualizer.model.strategy

import com.example.memoryvisualizer.model.MemoryBlock

/**
 * First Fit Strategy: Allocates the process to the first block it encounters
 * that is large enough to accommodate it.
 */
class FirstFitStrategy : AllocationStrategy {
    
    override fun chooseBlock(blocks: List<MemoryBlock>, processSize: Int): Int {
        // First Fit naturally implements the tie-breaking rule (lowest address first)
        // because blocks are assumed to be sorted by start address
        // and we iterate through them in order
        blocks.forEachIndexed { index, block ->
            // Return the index of first free block that can fit the process
            if (block.isFree && block.size >= processSize) {
                return index
            }
        }
        
        return -1  // No suitable block found
    }
    
    override val name: String = "First Fit"
}
package com.example.memoryvisualizer.model

import com.example.memoryvisualizer.model.strategy.AllocationStrategy

/**
 * Handles memory compaction operations to consolidate free space.
 * Repacks allocated memory blocks together and merges all free space into one block.
 */
class CompactionManager {
    
    /**
     * Compacts memory by moving allocated blocks to the lowest addresses and
     * consolidating all free space into a single block at the end.
     * Optionally retries allocating waiting processes after compaction.
     * 
     * @param blocks Current memory blocks (allocated and free)
     * @param processes List of processes (allocated, waiting, failed)
     * @param retryStrategy Optional strategy to retry allocating waiting/failed processes
     * @param allocationCallback Callback to handle allocation when a process fits after compaction
     * @return New list of memory blocks after compaction
     */
    fun compact(
        blocks: List<MemoryBlock>,
        processes: MutableList<ProcessDef>,
        retryStrategy: AllocationStrategy? = null,
        allocationCallback: (blockIndex: Int, processIndex: Int) -> Unit
    ): List<MemoryBlock> {
        
        // 1. Separate allocated blocks and sort them by start address
        val allocated = blocks.filter { !it.isFree }.sortedBy { it.start }
        val totalMemory = blocks.sumOf { it.size }
        
        // 2. Move allocated blocks to lowest addresses
        var cursor = 0
        val compactedBlocks = mutableListOf<MemoryBlock>()
        
        for (alloc in allocated) {
            compactedBlocks.add(
                alloc.copy(start = cursor)
            )
            cursor += alloc.size
        }
        
        // 3. Merge all free space into one tail block
        val freeSize = totalMemory - cursor
        if (freeSize > 0) {
            compactedBlocks.add(
                MemoryBlock(id = "FREE", start = cursor, size = freeSize, isFree = true)
            )
        }
        
        // 4. Optional: retry waiting/failed processes
        if (retryStrategy != null) {
            for (i in processes.indices) {
                val p = processes[i]
                if (p.status != ProcessStatus.ALLOCATED) {
                    val chosenIdx = retryStrategy.chooseBlock(compactedBlocks, p.size)
                    if (chosenIdx != -1) {
                        // Use callback to handle allocation logic
                        allocationCallback(chosenIdx, i)
                    }
                }
            }
        }
        
        return compactedBlocks
    }
    
    /**
     * Calculates fragmentation metrics after compaction.
     * 
     * @param blocks List of memory blocks after compaction
     * @return FragmentationStats containing metrics like external fragmentation
     */
    fun calculateFragmentationStats(blocks: List<MemoryBlock>): FragmentationStats {
        val free = blocks.filter { it.isFree }
        val totalFree = free.sumOf { it.size }
        val largestFree = free.maxOfOrNull { it.size } ?: 0
        val external = (totalFree - largestFree).coerceAtLeast(0)
        val holes = free.size
        
        // After proper compaction, external should be 0 and holes should be 0 or 1
        return FragmentationStats(
            internalTotal = 0, // We don't track internal fragmentation here
            externalTotal = external,
            largestFree = largestFree,
            holeCount = holes
        )
    }
    
    /**
     * Checks if memory is already compacted (no external fragmentation).
     * 
     * @param blocks List of memory blocks to check
     * @return True if memory is already compacted (one or zero free blocks)
     */
    fun isAlreadyCompacted(blocks: List<MemoryBlock>): Boolean {
        val freeBlocks = blocks.filter { it.isFree }
        return freeBlocks.size <= 1
    }
}
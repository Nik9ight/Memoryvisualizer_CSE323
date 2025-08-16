package com.example.memoryvisualizer.model

data class MemoryBlock(
    val id: String,
    val start: Int,      // start address
    val size: Int,       // block size
    val isFree: Boolean
) {
    /**
     * Returns the end address of this block (exclusive).
     */
    val end: Int get() = start + size
    
    /**
     * Extracts the process ID from this block's ID if it's allocated.
     * Returns null if the block is free or doesn't contain process info.
     */
    fun getProcessId(): String? {
        return if (!isFree && id.contains(":")) {
            id.substringAfter(":").takeIf { it != "L" }
        } else null
    }
    
    /**
     * Checks if this block is adjacent to another block.
     */
    fun isAdjacentTo(other: MemoryBlock): Boolean {
        return this.end == other.start || other.end == this.start
    }
    
    /**
     * Checks if this block can accommodate a process of given size.
     */
    fun canFit(processSize: Int): Boolean {
        return isFree && size >= processSize
    }
    
    /**
     * Checks if this is an allocated block.
     */
    val isAllocated: Boolean get() = !isFree
    
    /**
     * Creates a copy with updated free status.
     */
    fun withFreeStatus(free: Boolean): MemoryBlock {
        return copy(isFree = free)
    }
}
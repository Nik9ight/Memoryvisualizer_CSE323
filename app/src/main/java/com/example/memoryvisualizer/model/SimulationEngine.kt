package com.example.memoryvisualizer.model

import com.example.memoryvisualizer.model.strategy.AllocationStrategy
import com.example.memoryvisualizer.model.strategy.BestFitStrategy

/**
 * Core engine that manages the memory allocation simulation.
 * Handles block manipulation, process allocation, and fragmentation tracking.
 */
internal class SimulationEngine {
    private val blocks = mutableListOf<MemoryBlock>()    // sorted by start address
    private val processes = mutableListOf<ProcessDef>()  // order = allocation order
    private var nextProcessIdx = 0                       // pointer to next WAITING process
    private var lastAction: String = ""
    private var internalFragTotal = 0                    // tracks internal fragmentation
    
    // Original inputs for reset functionality
    private var originalBlocks = listOf<Int>()
    private var originalProcesses = listOf<Int>()
    
    // History management for undo/redo functionality
    private val snapshots = mutableListOf<AllocationResult>()
    private var cursor = -1                              // points to current snapshot
    
    /**
     * Loads the initial memory configuration and process list.
     */
    fun load(initialBlocks: List<Int>, processes: List<Int>) {
        // Store originals for reset
        originalBlocks = initialBlocks.filter { it > 0 }
        originalProcesses = processes.filter { it > 0 }
        
        // Reset state
        blocks.clear()
        this.processes.clear()
        nextProcessIdx = 0
        internalFragTotal = 0
        snapshots.clear()
        cursor = -1
        
        // Create initial free blocks
        var addr = 0
        originalBlocks.forEachIndexed { i, size ->
            blocks += MemoryBlock(id = "B$i", start = addr, size = size, isFree = true)
            addr += size
        }
        
        // Create initial process definitions
        originalProcesses.forEachIndexed { i, size ->
            this.processes += ProcessDef(id = "P${i+1}", size = size, status = ProcessStatus.WAITING)
        }
        
        lastAction = "LOAD"
        
        // Save initial snapshot
        saveSnapshot()
    }
    
    /**
     * Performs one allocation step using the provided strategy.
     * Returns a snapshot of the system state after the step.
     */
    fun step(strategy: AllocationStrategy): AllocationResult {
        // Find next WAITING process
        val pIdx = findNextWaiting()
        if (pIdx == -1) return snapshot("NO-OP: ALL DONE")
        val p = processes[pIdx]
        
        // Choose a block using the allocation strategy (pure)
        val chosen = strategy.chooseBlock(blocks, p.size)
        
        return if (chosen == -1) {
            // Mark as FAILED if no suitable block found
            processes[pIdx] = p.copy(status = ProcessStatus.FAILED)
            lastAction = "FAIL ${p.id} via ${strategy.name}"
            saveSnapshot()
            snapshot(lastAction)
        } else {
            // Split the chosen block and allocate the process
            splitAndAllocate(chosen, pIdx)
            lastAction = "ALLOCATE ${p.id} via ${strategy.name}"
            // Advance pointer for next call
            nextProcessIdx = pIdx + 1
            saveSnapshot()
            snapshot(lastAction)
        }
    }
    
    /**
     * Executes the simulation to completion, allocating all processes.
     * Returns a list of state snapshots after each step.
     */
    fun runAll(strategy: AllocationStrategy): List<AllocationResult> {
        val results = mutableListOf<AllocationResult>()
        
        while (true) {
            val pIdx = findNextWaiting()
            if (pIdx == -1) break // No more waiting processes
            
            val result = step(strategy)
            results.add(result)
        }
        
        return results
    }
    
    // CompactionManager instance for memory compaction operations
    private val compactionManager = CompactionManager()
    
    /**
     * Performs memory compaction by moving allocated blocks to the start
     * and consolidating all free space at the end.
     * Attempts to allocate waiting processes after compaction.
     */
    fun compact(): AllocationResult {
        // Use CompactionManager to handle the compaction logic
        val retryStrategy = BestFitStrategy() // BestFit is intuitive after compaction
        
        // Track if any allocations happen during compaction
        var changed = false
        
        // Perform compaction with a callback for allocation
        val compactedBlocks = compactionManager.compact(
            blocks = blocks,
            processes = processes,
            retryStrategy = retryStrategy
        ) { blockIndex, processIndex ->
            // This callback is invoked when a process can be allocated after compaction
            splitAndAllocate(blockIndex, processIndex)
            changed = true
        }
        
        // Update blocks with the new layout
        blocks.clear()
        blocks.addAll(compactedBlocks)
        coalesceFree() // Ensure free blocks are merged
        
        lastAction = if (changed) "COMPACT+REALLOC" else "COMPACT"
        saveSnapshot()
        return snapshot(lastAction)
    }
    
    /**
     * Resets the simulation to its initial state.
     */
    fun reset(): AllocationResult {
        load(originalBlocks, originalProcesses)
        return current() // load() already saves snapshot, so just return current
    }
    
    /**
     * Returns the current state of the simulation without changes.
     */
    fun current(): AllocationResult = snapshots.getOrElse(cursor) { snapshot("CURRENT") }
    
    /**
     * Splits a free block and allocates a portion to a process.
     */
    private fun splitAndAllocate(chosenIdx: Int, processIdx: Int) {
        val block = blocks[chosenIdx]
        check(block.isFree) { "Chosen block must be free" }
        val p = processes[processIdx]
        check(block.size >= p.size) { "Block too small" }
        
        // Create allocated block with process size
        val allocated = MemoryBlock(
            id = "${block.id}:${p.id}",
            start = block.start,
            size = p.size,
            isFree = false
        )
        
        // Calculate leftover size
        val leftoverSize = block.size - p.size
        
        // In our implementation with variable-sized blocks that are split,
        // there's no internal fragmentation (leftover becomes external fragmentation)
        // Internal fragmentation would only happen in fixed partition schemes
        // So we don't increment internalFragTotal here
        
        // Replace chosen block with allocated + optional leftover, preserving order
        blocks.removeAt(chosenIdx)
        if (leftoverSize > 0) {
            // First add the allocated block (which starts at the original block's start)
            blocks.add(chosenIdx, allocated)
            
            // Then add the leftover block (which comes after the allocated block)
            val leftover = MemoryBlock(
                id = "${block.id}:L", 
                start = block.start + p.size, 
                size = leftoverSize, 
                isFree = true
            )
            blocks.add(chosenIdx + 1, leftover)
        } else {
            // Exact fit, just add the allocated block
            blocks.add(chosenIdx, allocated)
        }
        
        // Update process status & link
        processes[processIdx] = p.copy(
            status = ProcessStatus.ALLOCATED, 
            allocatedBlockId = allocated.id
        )
    }
    
    /**
     * Merges adjacent free blocks.
     */
    private fun coalesceFree() {
        if (blocks.isEmpty()) return
        
        // Ensure blocks are sorted by start address
        blocks.sortBy { it.start }
        
        var i = 0
        while (i < blocks.size - 1) {
            val a = blocks[i]
            val b = blocks[i + 1]
            
            if (a.isFree && b.isFree && a.start + a.size == b.start) {
                // Merge adjacent free blocks
                blocks[i] = a.copy(size = a.size + b.size)
                blocks.removeAt(i + 1)
            } else {
                i++
            }
        }
    }
    
    /**
     * Computes fragmentation statistics based on current state.
     */
    private fun recomputeStats(): FragmentationStats {
        val free = blocks.filter { it.isFree }
        val totalFree = free.sumOf { it.size }
        val largestFree = free.maxOfOrNull { it.size } ?: 0
        val external = (totalFree - largestFree).coerceAtLeast(0)
        val holes = free.size
        
        return FragmentationStats(
            internalTotal = internalFragTotal,
            externalTotal = external,
            largestFree = largestFree,
            holeCount = holes
        )
    }
    
    /**
     * Creates an immutable snapshot of the current state.
     */
    private fun snapshot(action: String): AllocationResult {
        val blocksCopy = blocks.map { it.copy() }
        val procsCopy = processes.map { it.copy() }
        
        return AllocationResult(
            blocks = blocksCopy,
            processes = procsCopy,
            stats = recomputeStats(),
            lastAction = action
        )
    }
    
    /**
     * Finds the next waiting process starting from a specified index.
     */
    private fun findNextWaiting(startFrom: Int = nextProcessIdx): Int {
        // First look from the starting point to the end
        for (i in startFrom until processes.size) {
            if (processes[i].status == ProcessStatus.WAITING) return i
        }
        
        // If not found, look from beginning to starting point
        for (i in 0 until startFrom) {
            if (processes[i].status == ProcessStatus.WAITING) return i
        }
        
        return -1 // No waiting processes found
    }
    
    /**
     * Moves back to the previous snapshot in history.
     */
    fun undo(): AllocationResult? {
        if (cursor > 0) {
            cursor--
            restoreFromSnapshot()
            return current()
        }
        return null
    }
    
    /**
     * Moves forward to the next snapshot in history.
     */
    fun redo(): AllocationResult? {
        if (cursor < snapshots.lastIndex) {
            cursor++
            restoreFromSnapshot()
            return current()
        }
        return null
    }
    
    /**
     * Checks if undo operation is possible.
     */
    fun canUndo(): Boolean = cursor > 0
    
    /**
     * Checks if redo operation is possible.
     */
    fun canRedo(): Boolean = cursor < snapshots.lastIndex
    
    /**
     * Saves the current state as a snapshot and truncates forward history.
     */
    private fun saveSnapshot() {
        // Truncate forward history if stepping after undo
        while (snapshots.lastIndex > cursor) {
            snapshots.removeAt(snapshots.lastIndex)
        }
        
        val snapshot = snapshot(lastAction)
        snapshots.add(snapshot)
        cursor = snapshots.lastIndex
    }
    
    /**
     * Restores the engine state from the current snapshot.
     */
    private fun restoreFromSnapshot() {
        val currentSnapshot = snapshots.getOrNull(cursor) ?: return
        
        // Restore blocks
        blocks.clear()
        blocks.addAll(currentSnapshot.blocks.map { it.copy() })
        
        // Restore processes
        processes.clear()
        processes.addAll(currentSnapshot.processes.map { it.copy() })
        
        // Restore other state
        lastAction = currentSnapshot.lastAction
        internalFragTotal = currentSnapshot.stats.internalTotal
        
        // Recalculate nextProcessIdx
        nextProcessIdx = processes.indexOfFirst { it.status == ProcessStatus.WAITING }
        if (nextProcessIdx == -1) {
            nextProcessIdx = processes.size
        }
    }
}
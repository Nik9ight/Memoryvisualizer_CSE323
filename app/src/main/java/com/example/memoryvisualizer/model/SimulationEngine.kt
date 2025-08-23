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
    private var currentTime = 0                          // simulation time
    private val allocationTimes = mutableMapOf<String, Int>() // processId -> allocation time
    
    // Original inputs for reset functionality
    private var originalBlocks = listOf<Int>()
    private var originalProcesses = listOf<Int>()
    private var originalArrivals: List<Int>? = null
    private var originalBursts: List<Int>? = null
    
    // History management for undo/redo functionality
    private val snapshots = mutableListOf<AllocationResult>()
    private var cursor = -1                              // points to current snapshot
    private val timeSnapshots = mutableListOf<Int>()     // parallel time tracking
    private val allocationTimeSnapshots = mutableListOf<Map<String, Int>>() // parallel allocation time tracking
    
    /**
     * Loads the initial memory configuration and process list.
     */
    fun load(initialBlocks: List<Int>, processes: List<Int>) {
        load(initialBlocks, processes, null, null)
    }
    
    /**
     * Loads the initial memory configuration and process list with optional arrival and burst times.
     */
    fun load(initialBlocks: List<Int>, processes: List<Int>, arrivals: List<Int>?, bursts: List<Int>?) {
        // Store originals for reset
        originalBlocks = initialBlocks.filter { it > 0 }
        originalProcesses = processes.filter { it > 0 }
        originalArrivals = arrivals
        originalBursts = bursts
        
        // Reset state
        blocks.clear()
        this.processes.clear()
        nextProcessIdx = 0
        internalFragTotal = 0
        snapshots.clear()
        timeSnapshots.clear()
        allocationTimeSnapshots.clear()
        cursor = -1
        currentTime = 0
        allocationTimes.clear()
        
        // Create initial free blocks
        var addr = 0
        originalBlocks.forEachIndexed { i, size ->
            blocks += MemoryBlock(id = "B$i", start = addr, size = size, isFree = true)
            addr += size
        }
        
        // Create initial process definitions
        originalProcesses.forEachIndexed { i, size ->
            val arrival = arrivals?.getOrNull(i) ?: 0
            val burst = bursts?.getOrNull(i)
            val burstOrNull = burst?.takeIf { it > 0 } // <=0 treated as null
            this.processes += ProcessDef(
                id = "P${i+1}", 
                size = size, 
                status = ProcessStatus.WAITING,
                arrivalTime = arrival.coerceAtLeast(0),
                burstTime = burstOrNull,
                remainingBurst = burstOrNull
            )
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
        // First, handle burst time completions (auto-free processes)
        handleBurstCompletions()
        
        // Find next available process (considering arrival time)
        val pIdx = findNextAvailableProcess()
        if (pIdx == -1) {
            // No more processes available, advance time if there are future arrivals
            val nextArrivalTime = findNextArrivalTime()
            if (nextArrivalTime > currentTime) {
                currentTime = nextArrivalTime
                lastAction = "TIME ADVANCE to $currentTime"
                saveSnapshot()
                return step(strategy) // Retry allocation at new time
            }
            return snapshot("NO-OP: ALL DONE")
        }
        
        val p = processes[pIdx]
        
        // Advance time to process arrival if needed
        if (currentTime < p.arrivalTime) {
            currentTime = p.arrivalTime
        }
        
        // Choose a block using the allocation strategy (pure)
        val chosen = strategy.chooseBlock(blocks, p.size)
        
        return if (chosen == -1) {
            // Mark as FAILED if no suitable block found
            processes[pIdx] = p.copy(status = ProcessStatus.FAILED)
            lastAction = "FAIL ${p.id} via ${strategy.name} at time $currentTime"
            saveSnapshot()
            snapshot(lastAction)
        } else {
            // Split the chosen block and allocate the process
            splitAndAllocate(chosen, pIdx)
            allocationTimes[p.id] = currentTime
            
            // Create action message with scheduling info
            val burstInfo = if (p.burstTime != null) " (burst: ${p.burstTime})" else ""
            lastAction = "ALLOCATE ${p.id} via ${strategy.name} at time $currentTime$burstInfo"
            
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
            // Check if there are any processes that can still be processed
            val hasWaitingProcesses = processes.any { it.status == ProcessStatus.WAITING }
            val hasAllocatedWithBurstTime = processes.any { 
                it.status == ProcessStatus.ALLOCATED && it.burstTime != null 
            }
            
            if (!hasWaitingProcesses && !hasAllocatedWithBurstTime) {
                break // No more processes to handle
            }
            
            val result = step(strategy)
            results.add(result)
            
            // Prevent infinite loops
            if (results.size > 1000) {
                lastAction = "SIMULATION TIMEOUT"
                break
            }
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
        load(originalBlocks, originalProcesses, originalArrivals, originalBursts)
        return current() // load() already saves snapshot, so just return current
    }
    
    /**
     * Returns the current state of the simulation without changes.
     */
    fun current(): AllocationResult = snapshots.getOrElse(cursor) { snapshot("CURRENT") }
    
    /**
     * Handles burst time completions - frees processes that have exceeded their burst time.
     */
    private fun handleBurstCompletions() {
        val processesToFree = mutableListOf<Int>()
        
        for (i in processes.indices) {
            val p = processes[i]
            if (p.status == ProcessStatus.ALLOCATED) {
                val allocationTime = allocationTimes[p.id] ?: 0
                if (p.shouldAutoFree(currentTime, allocationTime)) {
                    processesToFree.add(i)
                }
            }
        }
        
        // Free the processes and their blocks
        for (processIdx in processesToFree) {
            val p = processes[processIdx]
            // Find and free the allocated block
            val blockIdx = blocks.indexOfFirst { it.id == p.allocatedBlockId }
            if (blockIdx != -1) {
                blocks[blockIdx] = blocks[blockIdx].copy(isFree = true)
                coalesceFree() // Merge adjacent free blocks
            }
            // Mark process as completed
            processes[processIdx] = p.markCompleted()
            allocationTimes.remove(p.id)
        }
    }
    
    /**
     * Finds the next process that has arrived and is waiting.
     * When multiple processes arrive at the same time, prioritizes the one with the shortest burst time.
     */
    private fun findNextAvailableProcess(): Int {
        // Get all available processes (arrived and waiting)
        val availableProcesses = processes.indices.filter { i ->
            val p = processes[i]
            p.status == ProcessStatus.WAITING && p.hasArrived(currentTime)
        }
        
        if (availableProcesses.isEmpty()) {
            return -1
        }
        
        // Group processes by arrival time and find the earliest arrival time
        val processesGroupedByArrival = availableProcesses.groupBy { processes[it].arrivalTime }
        val earliestArrivalTime = processesGroupedByArrival.keys.minOrNull() ?: return -1
        
        // Get processes that arrived at the earliest time
        val earliestArrivedProcesses = processesGroupedByArrival[earliestArrivalTime] ?: return -1
        
        // If only one process arrived at the earliest time, return it
        if (earliestArrivedProcesses.size == 1) {
            return earliestArrivedProcesses[0]
        }
        
        // Multiple processes arrived at the same time - apply shortest burst time tie-breaking
        return earliestArrivedProcesses.minWithOrNull { idx1, idx2 ->
            val p1 = processes[idx1]
            val p2 = processes[idx2]
            
            // Compare burst times (null burst time is treated as infinity - lowest priority)
            when {
                p1.burstTime == null && p2.burstTime == null -> {
                    // Both have no burst time, use original order (process ID)
                    idx1.compareTo(idx2)
                }
                p1.burstTime == null -> 1  // p1 has no burst time, p2 wins
                p2.burstTime == null -> -1 // p2 has no burst time, p1 wins
                else -> {
                    // Both have burst times, shortest wins
                    val burstCompare = p1.burstTime.compareTo(p2.burstTime)
                    if (burstCompare == 0) {
                        // Same burst time, use original order (process ID)
                        idx1.compareTo(idx2)
                    } else {
                        burstCompare
                    }
                }
            }
        } ?: -1
    }
    
    /**
     * Finds the next arrival time in the future.
     */
    private fun findNextArrivalTime(): Int {
        return processes
            .filter { it.status == ProcessStatus.WAITING && it.arrivalTime > currentTime }
            .minOfOrNull { it.arrivalTime } ?: Int.MAX_VALUE
    }
    
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
            timeSnapshots.removeAt(timeSnapshots.lastIndex)
            allocationTimeSnapshots.removeAt(allocationTimeSnapshots.lastIndex)
        }
        
        val snapshot = snapshot(lastAction)
        snapshots.add(snapshot)
        timeSnapshots.add(currentTime)
        allocationTimeSnapshots.add(allocationTimes.toMap())
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
        
        // Restore time state
        currentTime = timeSnapshots.getOrNull(cursor) ?: 0
        allocationTimes.clear()
        allocationTimes.putAll(allocationTimeSnapshots.getOrNull(cursor) ?: emptyMap())
        
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
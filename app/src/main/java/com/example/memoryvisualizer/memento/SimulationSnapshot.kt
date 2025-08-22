package com.example.memoryvisualizer.memento

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.MemoryBlock
import com.example.memoryvisualizer.model.ProcessDef
import com.example.memoryvisualizer.model.FragmentationStats

/**
 * Represents an immutable snapshot of the simulation state (Memento).
 * Used for state restoration and timeline navigation.
 */
data class SimulationSnapshot(
    val blocks: List<MemoryBlock>,
    val processes: List<ProcessDef>,
    val stats: FragmentationStats,
    val lastAction: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Creates a snapshot from an AllocationResult
         */
        fun from(result: AllocationResult): SimulationSnapshot {
            return SimulationSnapshot(
                blocks = result.blocks.map { it.copy() },
                processes = result.processes.map { it.copy() },
                stats = result.stats,
                lastAction = result.lastAction
            )
        }
    }

    /**
     * Converts this snapshot back to an AllocationResult
     */
    fun toAllocationResult(): AllocationResult {
        return AllocationResult(
            blocks = blocks,
            processes = processes,
            stats = stats,
            lastAction = lastAction
        )
    }
}

package com.example.memoryvisualizer.persistence

import com.example.memoryvisualizer.model.MemoryBlock
import com.example.memoryvisualizer.model.ProcessDef
import com.example.memoryvisualizer.model.FragmentationStats
import com.example.memoryvisualizer.model.AllocationResult
import java.io.Serializable

/**
 * Represents a serializable snapshot of the simulation state.
 * This class is used for persisting simulation states.
 */
data class SimulationSnapshot(
    val blocks: List<MemoryBlock>,
    val processes: List<ProcessDef>,
    val stats: FragmentationStats,
    val lastAction: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    companion object {
        fun from(result: AllocationResult): SimulationSnapshot {
            return SimulationSnapshot(
                blocks = result.blocks.map { it.copy() },
                processes = result.processes.map { it.copy() },
                stats = result.stats,
                lastAction = result.lastAction
            )
        }
    }
}

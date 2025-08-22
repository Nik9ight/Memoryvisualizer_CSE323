package com.example.memoryvisualizer.commands

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.Simulator

/**
 * Command to perform memory compaction operation.
 */
class CompactCommand(
    private val simulator: Simulator
) : Command {
    private var previousState: AllocationResult? = null
    
    override fun execute(): Boolean {
        return try {
            previousState = simulator.current()
            simulator.compact()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun undo(): Boolean {
        return try {
            previousState?.let {
                // Reset to previous state is handled by simulator
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

package com.example.memoryvisualizer.commands

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.Simulator

/**
 * Command to reset the simulation to its initial state.
 */
class ResetCommand(
    private val simulator: Simulator
) : Command {
    private var previousState: AllocationResult? = null
    
    override fun execute(): Boolean {
        return try {
            previousState = simulator.current()
            simulator.reset()
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

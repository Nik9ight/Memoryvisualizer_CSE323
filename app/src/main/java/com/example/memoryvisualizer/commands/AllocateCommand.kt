package com.example.memoryvisualizer.commands

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.Simulator
import com.example.memoryvisualizer.model.strategy.AllocationStrategy

/**
 * Command to perform a memory allocation step using the current strategy.
 */
class AllocateCommand(
    private val simulator: Simulator,
    private val blockSizes: List<Int>? = null,
    private val processSizes: List<Int>? = null,
    private val strategy: AllocationStrategy? = null
) : Command {
    private var previousState: AllocationResult? = null
    private var currentStrategy: AllocationStrategy? = null
    private var wasLoaded = false
    
    override fun execute(): Boolean {
        try {
            previousState = simulator.current()
            
            if (blockSizes != null && processSizes != null) {
                simulator.load(blockSizes, processSizes)
                wasLoaded = true
            }
            
            if (strategy != null) {
                currentStrategy = strategy
                simulator.setStrategy(strategy)
            }
            
            simulator.step()
            return true
        } catch (e: Exception) {
            // Rollback any changes on failure
            if (currentStrategy != null) {
                simulator.setStrategy(currentStrategy!!)
            }
            if (wasLoaded && previousState != null) {
                val prevBlockSizes = previousState!!.blocks.map { it.size }
                val prevProcessSizes = previousState!!.processes.map { it.size }
                simulator.load(prevBlockSizes, prevProcessSizes)
            }
            return false
        }
    }

    override fun undo(): Boolean {
        try {
            if (previousState != null) {
                if (wasLoaded) {
                    val prevBlockSizes = previousState!!.blocks.map { it.size }
                    val prevProcessSizes = previousState!!.processes.map { it.size }
                    simulator.load(prevBlockSizes, prevProcessSizes)
                }
                if (currentStrategy != null) {
                    simulator.setStrategy(currentStrategy!!)
                }
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}

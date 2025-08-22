package com.example.memoryvisualizer.model

import com.example.memoryvisualizer.memento.StateManager
import com.example.memoryvisualizer.memento.TimelineManager
import com.example.memoryvisualizer.model.strategy.AllocationStrategy

/**
 * Implementation of the Simulator interface that delegates to SimulationEngine.
 * Serves as a facade for the engine's operations and integrates with the Memento pattern.
 */
class SimulatorImpl(private var strategy: AllocationStrategy) : Simulator {
    private val engine = SimulationEngine()
    private val stateManager = StateManager()
    private val timelineManager = TimelineManager(stateManager)

    override fun load(initialBlocks: List<Int>, processes: List<Int>) {
        engine.load(initialBlocks, processes)
        val result = engine.current()
        stateManager.saveSnapshot(result)
    }

    override fun setStrategy(strategy: AllocationStrategy) {
        this.strategy = strategy
    }

    override fun step(): AllocationResult {
        val result = engine.step(strategy)
        stateManager.saveSnapshot(result)
        return result
    }

    override fun runAll(): List<AllocationResult> {
        val results = engine.runAll(strategy)
        results.forEach { stateManager.saveSnapshot(it) }
        return results
    }

    override fun compact(): AllocationResult {
        val result = engine.compact()
        stateManager.saveSnapshot(result)
        return result
    }

    override fun reset(): AllocationResult {
        val result = engine.reset()
        stateManager.clear()
        stateManager.saveSnapshot(result)
        return result
    }

    override fun current(): AllocationResult = engine.current()
    
    override fun undo(): AllocationResult? {
        val snapshot = stateManager.getPreviousSnapshot()
        return snapshot?.also { 
            engine.load(it.blocks.map { b -> b.size }, it.processes.map { p -> p.size })
        }?.toAllocationResult()
    }
    
    override fun redo(): AllocationResult? {
        val snapshot = stateManager.getNextSnapshot()
        return snapshot?.also {
            engine.load(it.blocks.map { b -> b.size }, it.processes.map { p -> p.size })
        }?.toAllocationResult()
    }
    
    override fun canUndo(): Boolean = stateManager.canMoveBack()
    
    override fun canRedo(): Boolean = stateManager.canMoveForward()

    /**
     * Gets the current timeline position
     */
    fun getTimelinePosition(): Int = timelineManager.currentPosition.value

    /**
     * Gets the total number of steps in the timeline
     */
    fun getTotalSteps(): Int = timelineManager.totalSteps.value

    /**
     * Moves to a specific position in the timeline
     */
    fun moveToTimelinePosition(position: Int): Boolean {
        return timelineManager.moveToPosition(position)
    }
}

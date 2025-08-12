package com.example.memoryvisualizer.model

import com.example.memoryvisualizer.model.strategy.AllocationStrategy

/**
 * Implementation of the Simulator interface that delegates to SimulationEngine.
 * Serves as a facade for the engine's operations.
 */
class SimulatorImpl(private var strategy: AllocationStrategy) : Simulator {
    private val engine = SimulationEngine()

    override fun load(initialBlocks: List<Int>, processes: List<Int>) {
        engine.load(initialBlocks, processes)
    }

    override fun setStrategy(strategy: AllocationStrategy) {
        this.strategy = strategy
    }

    override fun step(): AllocationResult = engine.step(strategy)

    override fun runAll(): List<AllocationResult> = engine.runAll(strategy)

    override fun compact(): AllocationResult = engine.compact()

    override fun reset(): AllocationResult = engine.reset()

    override fun current(): AllocationResult = engine.current()
}

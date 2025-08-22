package com.example.memoryvisualizer.stub

import com.example.memoryvisualizer.model.Simulator
import com.example.memoryvisualizer.model.SimulatorImpl
import com.example.memoryvisualizer.model.strategy.FirstFitStrategy
import com.example.memoryvisualizer.model.strategy.BestFitStrategy
import com.example.memoryvisualizer.model.strategy.WorstFitStrategy
import com.example.memoryvisualizer.model.strategy.AllocationStrategy
import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.MemoryBlock
import com.example.memoryvisualizer.model.ProcessDef
import com.example.memoryvisualizer.model.ProcessStatus
import com.example.memoryvisualizer.model.FragmentationStats
import com.example.memoryvisualizer.persistence.SimulationSnapshot

/**
 * Adapter that bridges the real SimulatorImpl with the UI-expected interface.
 * Now uses Person A's actual model classes as the foundation while maintaining UI compatibility.
 */
class SimulatorStub : Simulator {
    
    // UI-compatible data classes that extend/wrap Person A's model classes
    data class AllocationResultStub(
        val blocks: List<Int>,
        val processes: List<Int>,
        val allocations: List<Int>,
        val waitingProcesses: List<Int>,
        val action: String,
        val stats: Map<String, Int>
    ) {
        companion object {
            fun from(result: AllocationResult): AllocationResultStub {
                return AllocationResultStub(
                    blocks = result.blocks.map { it.size },
                    processes = result.processes.map { it.size },
                    allocations = result.allocatedBlocks.map { it.size },
                    waitingProcesses = result.waitingProcesses.map { it.size },
                    action = result.lastAction,
                    stats = mapOf(
                        "internalFragmentation" to result.stats.internalFragmentation,
                        "externalFragmentation" to result.stats.externalFragmentation,
                        "largestFreeBlock" to result.stats.largestFreeBlock,
                        "holes" to result.stats.holes,
                        "successRate" to result.successPercentage.toInt()
                    )
                )
            }
        }
    }
    
    /**
     * Restores the simulator state from a snapshot
     */
    private val realSimulator = SimulatorImpl()
    private var currentStrategy: AllocationStrategy = FirstFitStrategy()

    fun restoreFromSnapshot(result: AllocationResult): AllocationResultStub {
        realSimulator.load(
            initialBlocks = result.blocks.map { it.size },
            processes = result.processes.map { it.size }
        )
        return AllocationResultStub.from(realSimulator.current())
    }

    override fun setStrategy(strategy: AllocationStrategy) {
        currentStrategy = strategy
        realSimulator.setStrategy(strategy)
    }

    override fun load(blocks: List<Int>, processes: List<Int>) {
        realSimulator.load(blocks, processes)
    }

    override fun step(): AllocationResultStub {
        return AllocationResultStub.from(realSimulator.step())
    }

    override fun current(): AllocationResultStub {
        return AllocationResultStub.from(realSimulator.current())
    }

    override fun runAll(): List<AllocationResultStub> {
        return realSimulator.runAll().map { AllocationResultStub.from(it) }
    }

    override fun compact(): AllocationResultStub {
        return AllocationResultStub.from(realSimulator.compact())
    }

    override fun hasWaitingProcesses(): Boolean {
        return realSimulator.hasWaitingProcesses()
    }

    override fun getCurrentStrategy(): AllocationStrategy {
        return currentStrategy
    }
    
    data class BlockStub(
        val id: String,
        val start: Int,
        val size: Int,
        val isFree: Boolean,
        val processId: String? = null,
        val internalFrag: Int = 0
    ) {
        companion object {
            fun from(memoryBlock: MemoryBlock): BlockStub {
                return BlockStub(
                    id = memoryBlock.id,
                    start = memoryBlock.start,
                    size = memoryBlock.size,
                    isFree = memoryBlock.isFree,
                    processId = memoryBlock.getProcessId(),
                    internalFrag = 0 // Our implementation doesn't track internal fragmentation per block
                )
            }
        }
    }
    
    data class ProcessStub(
        val id: String,
        val size: Int,
        val status: Status,
        val allocatedBlockId: String? = null
    ) {
        enum class Status { ALLOCATED, WAITING, FAILED }
        
        companion object {
            fun from(processDef: ProcessDef): ProcessStub {
                return ProcessStub(
                    id = processDef.id,
                    size = processDef.size,
                    status = when(processDef.status) {
                        ProcessStatus.ALLOCATED -> Status.ALLOCATED
                        ProcessStatus.WAITING -> Status.WAITING
                        ProcessStatus.FAILED -> Status.FAILED
                    },
                    allocatedBlockId = processDef.allocatedBlockId
                )
            }
        }
    }
    
    data class StatsStub(
        val internalTotal: Int,
        val externalFree: Int,
        val largestFree: Int,
        val holeCount: Int,
        val successPct: Double
    ) {
        companion object {
            fun from(fragmentationStats: FragmentationStats, processes: List<ProcessDef>): StatsStub {
                val successPct = if (processes.isEmpty()) 0.0 else {
                    val allocated = processes.count { it.status == ProcessStatus.ALLOCATED }
                    allocated * 100.0 / processes.size
                }
                
                return StatsStub(
                    internalTotal = fragmentationStats.internalTotal,
                    externalFree = fragmentationStats.externalTotal,
                    largestFree = fragmentationStats.largestFree,
                    holeCount = fragmentationStats.holeCount,
                    successPct = successPct
                )
            }
        }
    }
    
    data class AllocationResultStub(
        val blocks: List<BlockStub>,
        val processes: List<ProcessStub>,
        val stats: StatsStub,
        val lastAction: String
    ) {
        companion object {
            fun from(allocationResult: AllocationResult): AllocationResultStub {
                return AllocationResultStub(
                    blocks = allocationResult.blocks.map { BlockStub.from(it) },
                    processes = allocationResult.processes.map { ProcessStub.from(it) },
                    stats = StatsStub.from(allocationResult.stats, allocationResult.processes),
                    lastAction = allocationResult.lastAction
                )
            }
        }
    }
    
    enum class Strategy { FIRST, BEST, WORST }

    // Real simulator instance
    private val realSimulator = SimulatorImpl(FirstFitStrategy())
    private var currentStrategy: Strategy = Strategy.FIRST

    /**
     * UI-specific load method that returns a stub result
     */
    fun loadWithStub(blocks: List<Int>, processes: List<Int>) : AllocationResultStub {
        realSimulator.load(blocks, processes)
        return AllocationResultStub.from(realSimulator.current())
    }

    /**
     * Gets the current allocation strategy
     */
    fun getCurrentStrategy(): AllocationStrategy = when(currentStrategy) {
        Strategy.FIRST -> FirstFitStrategy()
        Strategy.BEST -> BestFitStrategy()
        Strategy.WORST -> WorstFitStrategy()
    }

    /**
     * Checks if there are any waiting processes
     */
    fun hasWaitingProcesses(): Boolean {
        return realSimulator.current().processes.any { it.status == ProcessStatus.WAITING }
    }

    override fun setStrategy(strategy: AllocationStrategy) {
        try {
            currentStrategy = when(strategy) {
                is BestFitStrategy -> Strategy.BEST
                is WorstFitStrategy -> Strategy.WORST
                is FirstFitStrategy -> Strategy.FIRST
                else -> throw IllegalArgumentException("Unsupported strategy type: ${strategy.javaClass.simpleName}")
            }
            realSimulator.setStrategy(strategy)
        } catch (e: Exception) {
            // Fallback to FirstFit if strategy conversion fails
            currentStrategy = Strategy.FIRST
            realSimulator.setStrategy(FirstFitStrategy())
        }
    }

    override fun load(initialBlocks: List<Int>, processes: List<Int>) {
        realSimulator.load(initialBlocks, processes)
    }

    override fun step(): AllocationResult {
        try {
            return realSimulator.step()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to perform allocation step: ${e.message}")
        }
    }
    
    override fun runAll(): List<AllocationResult> {
        try {
            return realSimulator.runAll()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to run all allocations: ${e.message}")
        }
    }
    
    override fun compact(): AllocationResult {
        try {
            return realSimulator.compact()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to compact memory: ${e.message}")
        }
    }
    
    override fun reset(): AllocationResult {
        try {
            return realSimulator.reset()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to reset simulation: ${e.message}")
        }
    }
    
    override fun current(): AllocationResult {
        try {
            return realSimulator.current()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to get current state: ${e.message}")
        }
    }

    override fun undo(): AllocationResult? {
        try {
            return realSimulator.undo()
        } catch (e: Exception) {
            return null // Silently fail undo operations
        }
    }

    override fun redo(): AllocationResult? {
        try {
            return realSimulator.redo()
        } catch (e: Exception) {
            return null // Silently fail redo operations
        }
    }

    override fun canUndo(): Boolean = try {
        realSimulator.canUndo()
    } catch (e: Exception) {
        false
    }

    override fun canRedo(): Boolean = try {
        realSimulator.canRedo()
    } catch (e: Exception) {
        false
    }

    /**
     * Utility method to get the process ID from a memory block stub.
     */
    fun getProcessIdFromBlock(block: BlockStub): String? {
        return block.processId
    }
    
    /**
     * Utility method to calculate success percentage from process list.
     */
    fun calculateSuccessPercentage(processes: List<ProcessStub>): Double {
        if (processes.isEmpty()) return 0.0
        val allocated = processes.count { it.status == ProcessStub.Status.ALLOCATED }
        return allocated * 100.0 / processes.size
    }
    
    /**
     * Utility method to get total memory size from blocks.
     */
    fun getTotalMemorySize(blocks: List<BlockStub>): Int {
        return blocks.sumOf { it.size }
    }
}

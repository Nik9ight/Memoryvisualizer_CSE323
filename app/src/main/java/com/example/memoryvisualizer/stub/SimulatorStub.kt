package com.example.memoryvisualizer.stub

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

/**
 * Adapter that bridges the real SimulatorImpl with the UI-expected interface.
 * Now uses Person A's actual model classes as the foundation while maintaining UI compatibility.
 */
class SimulatorStub {
    
    // UI-compatible data classes that extend/wrap Person A's model classes
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
        val allocatedBlockId: String? = null,
        val arrivalTime: Int = 0,
        val burstTime: Int? = null,
        val remainingBurst: Int? = null
    ) {
        enum class Status { ALLOCATED, WAITING, FAILED, COMPLETED }
        
        companion object {
            fun from(processDef: ProcessDef): ProcessStub {
                return ProcessStub(
                    id = processDef.id,
                    size = processDef.size,
                    status = when(processDef.status) {
                        ProcessStatus.ALLOCATED -> Status.ALLOCATED
                        ProcessStatus.WAITING -> Status.WAITING
                        ProcessStatus.FAILED -> Status.FAILED
                        ProcessStatus.COMPLETED -> Status.COMPLETED
                    },
                    allocatedBlockId = processDef.allocatedBlockId,
                    arrivalTime = processDef.arrivalTime,
                    burstTime = processDef.burstTime,
                    remainingBurst = processDef.remainingBurst
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
        val action: String
    ) {
        companion object {
            fun from(allocationResult: AllocationResult): AllocationResultStub {
                return AllocationResultStub(
                    blocks = allocationResult.blocks.map { BlockStub.from(it) },
                    processes = allocationResult.processes.map { ProcessStub.from(it) },
                    stats = StatsStub.from(allocationResult.stats, allocationResult.processes),
                    action = allocationResult.lastAction
                )
            }
        }
    }
    
    enum class Strategy { FIRST, BEST, WORST }

    // Real simulator instance
    private val realSimulator = SimulatorImpl(FirstFitStrategy())
    private var currentStrategy: Strategy = Strategy.FIRST

    fun load(blocks: List<Int>, processes: List<Int>) : AllocationResultStub {
        realSimulator.load(blocks, processes)
        return AllocationResultStub.from(realSimulator.current())
    }

    fun load(blocks: List<Int>, processes: List<Int>, arrivals: List<Int>?, bursts: List<Int>?) : AllocationResultStub {
        realSimulator.load(blocks, processes, arrivals, bursts)
        return AllocationResultStub.from(realSimulator.current())
    }

    fun setStrategy(s: Strategy) {
        currentStrategy = s
        val strategy = when(s) {
            Strategy.FIRST -> FirstFitStrategy()
            Strategy.BEST -> BestFitStrategy()
            Strategy.WORST -> WorstFitStrategy()
        }
        realSimulator.setStrategy(strategy)
    }

    fun current(): AllocationResultStub? {
        return AllocationResultStub.from(realSimulator.current())
    }

    fun step(): AllocationResultStub? {
        return AllocationResultStub.from(realSimulator.step())
    }

    fun runAll(): AllocationResultStub? {
        val results = realSimulator.runAll()
        return if (results.isNotEmpty()) {
            AllocationResultStub.from(results.last())
        } else {
            AllocationResultStub.from(realSimulator.current())
        }
    }

    fun compact(): AllocationResultStub? {
        return AllocationResultStub.from(realSimulator.compact())
    }

    fun reset(): AllocationResultStub? {
        return AllocationResultStub.from(realSimulator.reset())
    }

    fun undo(): AllocationResultStub? {
        return realSimulator.undo()?.let { AllocationResultStub.from(it) }
    }

    fun redo(): AllocationResultStub? {
        return realSimulator.redo()?.let { AllocationResultStub.from(it) }
    }
    
    fun canUndo(): Boolean {
        return realSimulator.canUndo()
    }
    
    fun canRedo(): Boolean {
        return realSimulator.canRedo()
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

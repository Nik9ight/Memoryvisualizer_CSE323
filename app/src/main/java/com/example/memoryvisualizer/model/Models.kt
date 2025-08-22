package com.example.memoryvisualizer.model

data class MemoryBlock(
    val id: String,
    val start: Int,
    val size: Int,
    val isFree: Boolean = true,
    private val process: ProcessDef? = null
) {
    fun getProcessId(): String? = process?.id
    
    fun withProcess(process: ProcessDef?): MemoryBlock {
        return copy(
            isFree = process == null,
            process = process
        )
    }
}

data class ProcessDef(
    val id: String,
    val size: Int,
    val status: ProcessStatus = ProcessStatus.WAITING
) {
    val isWaiting: Boolean get() = status == ProcessStatus.WAITING
    val isAllocated: Boolean get() = status == ProcessStatus.ALLOCATED
    val hasFailed: Boolean get() = status == ProcessStatus.FAILED
}

enum class ProcessStatus {
    WAITING,
    ALLOCATED,
    FAILED
}

data class FragmentationStats(
    val internalFragmentation: Int = 0,
    val externalFragmentation: Int = 0,
    val largestFreeBlock: Int = 0,
    val holes: Int = 0,
    val successRate: Int = 0
) {
    fun calculateSuccessPercentage(processes: List<ProcessDef>): Double {
        if (processes.isEmpty()) return 0.0
        val allocated = processes.count { it.isAllocated }
        return (allocated.toDouble() / processes.size) * 100
    }
    
    fun getMemoryUtilization(totalMemory: Int): Double {
        if (totalMemory == 0) return 0.0
        val used = totalMemory - externalFragmentation - internalFragmentation
        return (used.toDouble() / totalMemory) * 100
    }
}

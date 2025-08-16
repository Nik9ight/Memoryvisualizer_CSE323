package com.example.memoryvisualizer.model

data class AllocationResult(
    val blocks: List<MemoryBlock>,
    val processes: List<ProcessDef>,
    val stats: FragmentationStats,
    val lastAction: String // e.g., "ALLOCATE P3 via BestFit"
) {
    /**
     * Gets all free blocks.
     */
    val freeBlocks: List<MemoryBlock> get() = blocks.filter { it.isFree }
    
    /**
     * Gets all allocated blocks.
     */
    val allocatedBlocks: List<MemoryBlock> get() = blocks.filter { !it.isFree }
    
    /**
     * Gets all waiting processes.
     */
    val waitingProcesses: List<ProcessDef> get() = processes.filter { it.isWaiting }
    
    /**
     * Gets all allocated processes.
     */
    val allocatedProcesses: List<ProcessDef> get() = processes.filter { it.isAllocated }
    
    /**
     * Gets all failed processes.
     */
    val failedProcesses: List<ProcessDef> get() = processes.filter { it.hasFailed }
    
    /**
     * Calculates success percentage.
     */
    val successPercentage: Double get() = stats.calculateSuccessPercentage(processes)
    
    /**
     * Gets total memory size.
     */
    val totalMemorySize: Int get() = blocks.sumOf { it.size }
    
    /**
     * Gets memory utilization percentage.
     */
    val memoryUtilization: Double get() = stats.getMemoryUtilization(totalMemorySize)
}
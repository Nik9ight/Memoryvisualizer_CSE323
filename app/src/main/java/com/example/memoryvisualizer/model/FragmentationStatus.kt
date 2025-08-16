package com.example.memoryvisualizer.model

data class FragmentationStats(
    val internalTotal: Int,
    val externalTotal: Int,
    val largestFree: Int,
    val holeCount: Int
) {
    /**
     * Calculate success percentage from a list of processes.
     */
    fun calculateSuccessPercentage(processes: List<ProcessDef>): Double {
        if (processes.isEmpty()) return 0.0
        val allocated = processes.count { it.status == ProcessStatus.ALLOCATED }
        return allocated * 100.0 / processes.size
    }
    
    /**
     * Total free memory calculation helper.
     */
    val totalFree: Int get() = externalTotal + largestFree
    
    /**
     * Memory utilization percentage (allocated / total).
     */
    fun getMemoryUtilization(totalMemorySize: Int): Double {
        if (totalMemorySize <= 0) return 0.0
        val allocated = totalMemorySize - totalFree
        return allocated * 100.0 / totalMemorySize
    }
}
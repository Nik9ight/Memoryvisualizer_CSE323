package com.example.memoryvisualizer.model

data class AllocationResult(
    val blocks: List<MemoryBlock>,
    val processes: List<ProcessDef>,
    val stats: FragmentationStats,
    val lastAction: String // e.g., "ALLOCATE P3 via BestFit"
)
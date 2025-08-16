package com.example.memoryvisualizer.model

enum class ProcessStatus { ALLOCATED, WAITING, FAILED }

data class ProcessDef(
    val id: String,
    val size: Int,
    val status: ProcessStatus = ProcessStatus.WAITING,
    val allocatedBlockId: String? = null
) {
    /**
     * Checks if this process is currently allocated.
     */
    val isAllocated: Boolean get() = status == ProcessStatus.ALLOCATED
    
    /**
     * Checks if this process is waiting for allocation.
     */
    val isWaiting: Boolean get() = status == ProcessStatus.WAITING
    
    /**
     * Checks if this process failed to be allocated.
     */
    val hasFailed: Boolean get() = status == ProcessStatus.FAILED
    
    /**
     * Creates a copy with updated status.
     */
    fun withStatus(newStatus: ProcessStatus): ProcessDef {
        return copy(status = newStatus)
    }
    
    /**
     * Creates a copy with allocation information.
     */
    fun allocatedTo(blockId: String): ProcessDef {
        return copy(status = ProcessStatus.ALLOCATED, allocatedBlockId = blockId)
    }
}
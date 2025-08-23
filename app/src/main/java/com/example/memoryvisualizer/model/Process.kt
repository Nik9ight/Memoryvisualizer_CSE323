package com.example.memoryvisualizer.model

enum class ProcessStatus { ALLOCATED, WAITING, FAILED, COMPLETED }

data class ProcessDef(
    val id: String,
    val size: Int,
    val status: ProcessStatus = ProcessStatus.WAITING,
    val allocatedBlockId: String? = null,
    val arrivalTime: Int = 0,          // default keeps old behavior
    val burstTime: Int? = null,        // null = never auto-free (old behavior)
    val remainingBurst: Int? = burstTime
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
     * Checks if this process has completed its burst time.
     */
    val isCompleted: Boolean get() = status == ProcessStatus.COMPLETED
    
    /**
     * Checks if this process has arrived (current time >= arrival time).
     */
    fun hasArrived(currentTime: Int): Boolean = currentTime >= arrivalTime
    
    /**
     * Checks if this process should be auto-freed based on burst time.
     */
    fun shouldAutoFree(currentTime: Int, allocationTime: Int): Boolean {
        return burstTime != null && (currentTime - allocationTime) >= burstTime
    }
    
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
    
    /**
     * Creates a copy with completed status.
     */
    fun markCompleted(): ProcessDef {
        return copy(status = ProcessStatus.COMPLETED)
    }
    
    /**
     * Creates a copy with updated remaining burst time.
     */
    fun withRemainingBurst(remaining: Int?): ProcessDef {
        return copy(remainingBurst = remaining)
    }
}
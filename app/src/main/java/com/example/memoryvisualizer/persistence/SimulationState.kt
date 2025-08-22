package com.example.memoryvisualizer.persistence

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.model.MemoryBlock
import com.example.memoryvisualizer.model.ProcessDef
import com.example.memoryvisualizer.model.ProcessStatus
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * A serializable snapshot of the simulation state.
 * Used for saving/loading simulation states and scenarios.
 */
data class SimulationState(
    @SerializedName("blocks") val blockSizes: List<Int>,
    @SerializedName("processes") val processSizes: List<Int>,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    companion object {
        fun from(result: AllocationResult, name: String, description: String = ""): SimulationState {
            return SimulationState(
                blockSizes = result.blocks.map { it.size },
                processSizes = result.processes.map { it.size },
                name = name,
                description = description
            )
        }
    }
}

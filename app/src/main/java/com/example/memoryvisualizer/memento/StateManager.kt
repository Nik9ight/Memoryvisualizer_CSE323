package com.example.memoryvisualizer.memento

import com.example.memoryvisualizer.model.AllocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages simulation state snapshots and provides timeline functionality.
 * Acts as the Caretaker in the Memento pattern.
 */
class StateManager {
    private val snapshots = mutableListOf<SimulationSnapshot>()
    private var currentIndex = -1

    private val _currentSnapshot = MutableStateFlow<SimulationSnapshot?>(null)
    val currentSnapshot: StateFlow<SimulationSnapshot?> = _currentSnapshot.asStateFlow()

    /**
     * Saves a new snapshot and truncates forward history
     */
    fun saveSnapshot(result: AllocationResult) {
        if (result.blocks.isEmpty() || result.processes.isEmpty()) {
            throw IllegalArgumentException("Cannot save snapshot with empty blocks or processes")
        }

        try {
            // Truncate forward history if we're not at the end
            while (snapshots.size - 1 > currentIndex) {
                snapshots.removeAt(snapshots.size - 1)
            }

            // Limit the number of snapshots to prevent memory issues
            if (snapshots.size >= MAX_SNAPSHOTS) {
                snapshots.removeAt(0)
                currentIndex--
            }

            val snapshot = SimulationSnapshot.from(result)
            snapshots.add(snapshot)
            currentIndex = snapshots.size - 1
            _currentSnapshot.value = snapshot
        } catch (e: Exception) {
            throw IllegalStateException("Failed to save snapshot: ${e.message}")
        }
    }

    companion object {
        private const val MAX_SNAPSHOTS = 1000 // Prevent memory leaks
    }

    /**
     * Moves to a specific point in the timeline
     * @return The snapshot at the specified index, or null if index is invalid
     */
    fun moveToSnapshot(index: Int): SimulationSnapshot? {
        if (index in 0..snapshots.lastIndex) {
            currentIndex = index
            val snapshot = snapshots[index]
            _currentSnapshot.value = snapshot
            return snapshot
        }
        return null
    }

    /**
     * Moves one step back in the timeline
     * @return The previous snapshot, or null if at the beginning
     */
    fun getPreviousSnapshot(): SimulationSnapshot? {
        return if (currentIndex > 0) {
            moveToSnapshot(currentIndex - 1)
        } else null
    }

    /**
     * Moves one step forward in the timeline
     * @return The next snapshot, or null if at the end
     */
    fun getNextSnapshot(): SimulationSnapshot? {
        return if (currentIndex < snapshots.lastIndex) {
            moveToSnapshot(currentIndex + 1)
        } else null
    }

    /**
     * Returns current timeline position
     */
    fun getCurrentPosition(): Int = currentIndex

    /**
     * Returns total number of snapshots
     */
    fun getSnapshotCount(): Int = snapshots.size

    /**
     * Checks if moving backward is possible
     */
    fun canMoveBack(): Boolean = currentIndex > 0

    /**
     * Checks if moving forward is possible
     */
    fun canMoveForward(): Boolean = currentIndex < snapshots.lastIndex

    /**
     * Clears all snapshots and resets the timeline
     */
    fun clear() {
        snapshots.clear()
        currentIndex = -1
        _currentSnapshot.value = null
    }

    /**
     * Gets a list of all available snapshots
     */
    fun getAllSnapshots(): List<SimulationSnapshot> = snapshots.toList()
}

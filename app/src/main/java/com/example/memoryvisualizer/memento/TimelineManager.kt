package com.example.memoryvisualizer.memento

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages timeline navigation and provides timeline-related information.
 */
class TimelineManager(
    private val stateManager: StateManager
) {
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    init {
        updateState()
    }

    /**
     * Moves to a specific position in the timeline
     * @return Success of the operation
     */
    fun moveToPosition(position: Int): Boolean {
        if (position < 0) return false
        
        return try {
            val snapshot = stateManager.moveToSnapshot(position)
            if (snapshot != null) {
                updateState()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Log error if needed
            false
        }
    }

    /**
     * Moves one step back in the timeline
     * @return Success of the operation
     */
    fun stepBack(): Boolean {
        val snapshot = stateManager.getPreviousSnapshot()
        if (snapshot != null) {
            updateState()
            return true
        }
        return false
    }

    /**
     * Moves one step forward in the timeline
     * @return Success of the operation
     */
    fun stepForward(): Boolean {
        val snapshot = stateManager.getNextSnapshot()
        if (snapshot != null) {
            updateState()
            return true
        }
        return false
    }

    /**
     * Jumps to the beginning of the timeline
     * @return Success of the operation
     */
    fun jumpToStart(): Boolean {
        return moveToPosition(0)
    }

    /**
     * Jumps to the end of the timeline
     * @return Success of the operation
     */
    fun jumpToEnd(): Boolean {
        return moveToPosition(stateManager.getSnapshotCount() - 1)
    }

    /**
     * Gets a description of the current timeline state
     */
    fun getTimelineInfo(): String {
        val current = stateManager.getCurrentPosition() + 1
        val total = stateManager.getSnapshotCount()
        return "Step $current of $total"
    }

    /**
     * Updates the internal state flows
     */
    private fun updateState() {
        _currentPosition.value = stateManager.getCurrentPosition()
        _totalSteps.value = stateManager.getSnapshotCount()
    }
}

package com.example.memoryvisualizer.commands

/**
 * Base interface for command pattern implementation.
 * Each command represents an atomic operation in the memory allocation simulation.
 */
interface Command {
    /**
     * Executes the command operation
     * @return Result of the operation
     */
    fun execute(): Boolean

    /**
     * Reverts the command operation
     * @return Success of the undo operation
     */
    fun undo(): Boolean
}

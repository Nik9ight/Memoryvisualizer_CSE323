package com.example.memoryvisualizer.commands

/**
 * Manages command execution and maintains command history for undo/redo operations.
 */
class CommandManager {
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()
    
    val position: Int
        get() = undoStack.size
        
    val commandCount: Int
        get() = undoStack.size + redoStack.size

    /**
     * Executes a command and adds it to the undo stack.
     * Clears redo stack as new operation invalidates redo history.
     * @param command The command to execute
     * @return Success of the operation
     */
    fun execute(command: Command): Boolean {
        val success = command.execute()
        if (success) {
            undoStack.add(command)
            redoStack.clear() // New command invalidates redo history
        }
        return success
    }

    /**
     * Undoes the last executed command.
     * @return Success of the undo operation
     */
    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        
        val command = undoStack.removeAt(undoStack.lastIndex)
        val success = command.undo()
        if (success) {
            redoStack.add(command)
        } else {
            undoStack.add(command) // Restore command if undo fails
        }
        return success
    }

    /**
     * Redoes the last undone command.
     * @return Success of the redo operation
     */
    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        
        val command = redoStack.removeAt(redoStack.lastIndex)
        val success = command.execute()
        if (success) {
            undoStack.add(command)
        } else {
            redoStack.add(command) // Restore command if redo fails
        }
        return success
    }

    /**
     * Checks if there are commands available to undo.
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**
     * Checks if there are commands available to redo.
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**
     * Clears all command history.
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

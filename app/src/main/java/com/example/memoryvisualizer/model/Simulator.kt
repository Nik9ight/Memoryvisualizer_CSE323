package com.example.memoryvisualizer.model

import com.example.memoryvisualizer.model.strategy.AllocationStrategy

interface Simulator {
    fun load(initialBlocks: List<Int>, processes: List<Int>)
    fun load(
        initialBlocks: List<Int>,
        processes: List<Int>,
        arrivals: List<Int>?,   // null -> default 0s
        bursts: List<Int>?      // null -> default nulls
    )
    fun setStrategy(strategy: AllocationStrategy)
    fun step(): AllocationResult              // allocate next waiting process
    fun runAll(): List<AllocationResult>      // simulate to end
    fun compact(): AllocationResult           // compaction + retry waiting
    fun reset(): AllocationResult
    fun current(): AllocationResult
    fun undo(): AllocationResult?             // move back to previous snapshot
    fun redo(): AllocationResult?             // move forward to next snapshot
    fun canUndo(): Boolean                    // check if undo is possible
    fun canRedo(): Boolean                    // check if redo is possible
}
package com.example.memoryvisualizer.model.strategy

import com.example.memoryvisualizer.model.MemoryBlock

public interface AllocationStrategy {
    /** Returns index of chosen free block or -1 if none fits */
    fun chooseBlock(blocks: List<MemoryBlock>, processSize: Int): Int
    val name: String
    fun allocate(blocks: List<Int>, processSize: Int): Int
}

 abstract class FirstFitStrategy : AllocationStrategy {
    override fun allocate(blocks: List<Int>, processSize: Int): Int {
        return blocks.indexOfFirst { it >= processSize }
    }
}


abstract class BestFitStrategy : AllocationStrategy {
    override fun allocate(blocks: List<Int>, processSize: Int): Int {
        var bestFitIndex = -1
        var bestFitSize = Int.MAX_VALUE

        blocks.forEachIndexed { index, blockSize ->
            if (blockSize >= processSize && blockSize < bestFitSize) {
                bestFitIndex = index
                bestFitSize = blockSize
            }
        }

        return bestFitIndex
    }
}

abstract class WorstFitStrategy : AllocationStrategy {
    override fun allocate(blocks: List<Int>, processSize: Int): Int {
        var worstFitIndex = -1
        var worstFitSize = -1

        blocks.forEachIndexed { index, blockSize ->
            if (blockSize >= processSize && blockSize > worstFitSize) {
                worstFitIndex = index
                worstFitSize = blockSize
            }
        }

        return worstFitIndex
    }
}

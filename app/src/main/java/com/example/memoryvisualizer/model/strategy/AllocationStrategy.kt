package com.example.memoryvisualizer.model.strategy

import com.example.memoryvisualizer.model.MemoryBlock

interface AllocationStrategy {
    /** Returns index of chosen free block or -1 if none fits */
    fun chooseBlock(blocks: List<MemoryBlock>, processSize: Int): Int
    val name: String
}
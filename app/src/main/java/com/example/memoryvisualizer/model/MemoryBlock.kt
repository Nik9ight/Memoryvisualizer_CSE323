package com.example.memoryvisualizer.model

data class MemoryBlock(
    val id: String,
    val start: Int,      // start address
    val size: Int,       // block size
    val isFree: Boolean
)
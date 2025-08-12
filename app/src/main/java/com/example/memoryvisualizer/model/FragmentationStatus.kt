package com.example.memoryvisualizer.model

data class FragmentationStats(
    val internalTotal: Int,
    val externalTotal: Int,
    val largestFree: Int,
    val holeCount: Int
)
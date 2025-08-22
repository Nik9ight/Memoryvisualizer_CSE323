package com.example.memoryvisualizer.model

data class Scenario(
    val name: String,
    val description: String,
    val blocks: List<Int>,
    val processes: List<Int>
)

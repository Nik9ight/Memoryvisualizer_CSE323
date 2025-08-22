package com.example.memoryvisualizer.persistence

import com.google.gson.annotations.SerializedName

data class Scenario(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("blocks") val blocks: List<Int>,
    @SerializedName("processes") val processes: List<Int>,
    @SerializedName("isBuiltIn") val isBuiltIn: Boolean = false
) {
    companion object {
        fun createSampleScenarios() = listOf(
            Scenario(
                name = "Basic Allocation",
                description = "Simple scenario with small blocks and processes",
                blocks = listOf(100, 50, 75, 25),
                processes = listOf(20, 40, 30, 10),
                isBuiltIn = true
            ),
            Scenario(
                name = "Fragmentation Example",
                description = "Scenario demonstrating external fragmentation",
                blocks = listOf(200, 150, 100, 50),
                processes = listOf(75, 125, 40, 60),
                isBuiltIn = true
            ),
            Scenario(
                name = "Complex Allocation",
                description = "Complex scenario with many blocks and processes",
                blocks = listOf(300, 200, 150, 100, 75, 50),
                processes = listOf(125, 75, 100, 50, 25, 175),
                isBuiltIn = true
            )
        )
    }
}

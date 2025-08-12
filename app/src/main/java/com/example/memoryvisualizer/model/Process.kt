package com.example.memoryvisualizer.model

enum class ProcessStatus { ALLOCATED, WAITING, FAILED }

data class ProcessDef(
    val id: String,
    val size: Int,
    val status: ProcessStatus = ProcessStatus.WAITING,
    val allocatedBlockId: String? = null
)
package com.example.memoryvisualizer.commands

import com.example.memoryvisualizer.model.AllocationResult
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel

class AllocateCommand(
    private val viewModel: VisualizerViewModel,
    private val beforeState: AllocationResult
) : Command {
    private var afterState: AllocationResult? = null
    
    override fun execute() {
        afterState = viewModel.onStepInternal()
    }
    
    override fun undo() {
        viewModel.restoreState(beforeState)
    }
}

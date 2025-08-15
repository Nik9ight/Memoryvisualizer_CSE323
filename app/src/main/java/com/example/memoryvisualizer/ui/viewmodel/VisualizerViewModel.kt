package com.example.memoryvisualizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryvisualizer.stub.SimulatorStub
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisualizerViewModel : ViewModel() {

    private val sim = SimulatorStub()

    private val _state = MutableStateFlow<SimulatorStub.AllocationResultStub?>(null)
    val state: StateFlow<SimulatorStub.AllocationResultStub?> = _state.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    private var lastInputBlocks: String = ""
    private var lastInputProcesses: String = ""

    fun onLoad(blocksCsv: String, processesCsv: String) {
        lastInputBlocks = blocksCsv
        lastInputProcesses = processesCsv
        parseCsv(blocksCsv)?.let { b ->
            parseCsv(processesCsv)?.let { p ->
                val res = sim.load(b, p)
                _state.value = res
            } ?: emitError("Invalid processes list")
        } ?: emitError("Invalid blocks list")
    }

    fun onStrategySelected(name: String) {
        val strat = when(name) {
            "Best Fit" -> SimulatorStub.Strategy.BEST
            "Worst Fit" -> SimulatorStub.Strategy.WORST
            else -> SimulatorStub.Strategy.FIRST
        }
        sim.setStrategy(strat)
    }

    fun onStep() { update(sim.step()) }
    fun onRun() { update(sim.runAll()) }
    fun onCompact() { update(sim.compact()) }
    fun onReset() { update(sim.reset()) }
    fun onUndo() { update(sim.undo()) }
    fun onRedo() { update(sim.redo()) }

    private fun update(res: SimulatorStub.AllocationResultStub?) { if (res!=null) _state.value = res }

    private fun parseCsv(csv: String): List<Int>? = try {
        csv.split(',', ';', ' ', '\n', '\t')
            .mapNotNull { token ->
                val t = token.trim()
                if (t.isEmpty()) null else t.toIntOrNull()?.takeIf { it > 0 }
            }
            .takeIf { it.isNotEmpty() }
    } catch (e: Exception) { null }

    private fun emitError(msg: String) { viewModelScope.launch { _errors.emit(msg) } }
}

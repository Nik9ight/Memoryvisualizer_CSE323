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

    private val _loaded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loaded: SharedFlow<Unit> = _loaded.asSharedFlow()

    private var lastInputBlocks: String = ""
    private var lastInputProcesses: String = ""
    private var lastInputArrivals: String = ""
    private var lastInputBursts: String = ""

    fun onLoad(blocksCsv: String, processesCsv: String) {
        onLoad(blocksCsv, processesCsv, null, null)
    }

    fun onLoad(
        blocksCsv: String, 
        processesCsv: String, 
        arrivalsCsv: String?, 
        burstsCsv: String?
    ) {
        lastInputBlocks = blocksCsv
        lastInputProcesses = processesCsv
        lastInputArrivals = arrivalsCsv ?: ""
        lastInputBursts = burstsCsv ?: ""
        
        parseCsv(blocksCsv)?.let { b ->
            parseCsv(processesCsv)?.let { p ->
                val arrivals = arrivalsCsv?.let { parseCsvAllowEmpty(it) }
                val bursts = burstsCsv?.let { parseCsvNullable(it) }
                
                // Validate lengths if provided
                if (arrivals != null && arrivals.size != p.size) {
                    emitError("Arrival list must match the number of processes")
                    return
                }
                if (bursts != null && bursts.size != p.size) {
                    emitError("Burst list must match the number of processes")
                    return
                }
                
                val burstsInts: List<Int>? = bursts?.map { it ?: -1 }?.let { list ->
                    if (list.all { it == -1 }) null else list.map { if (it < 0) 0 else it }
                }
                
                val res = sim.load(b, p, arrivals, burstsInts)
                _state.value = res
                _loaded.tryEmit(Unit)
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

    fun canUndo(): Boolean = sim.canUndo()
    fun canRedo(): Boolean = sim.canRedo()

    private fun update(res: SimulatorStub.AllocationResultStub?) { if (res!=null) _state.value = res }

    private fun parseCsv(csv: String): List<Int>? = try {
        csv.split(',', ';', ' ', '\n', '\t')
            .mapNotNull { token ->
                val t = token.trim()
                if (t.isEmpty()) null else t.toIntOrNull()?.takeIf { it > 0 }
            }
            .takeIf { it.isNotEmpty() }
    } catch (e: Exception) { null }

    /** Returns null if the entire field is blank; otherwise a list (default 0 for blank tokens). */
    private fun parseCsvAllowEmpty(input: String): List<Int>? {
        val raw = input.trim()
        if (raw.isEmpty()) return null
        val tokens = raw.split(',', ';', ' ', '\n', '\t')
        return tokens.map { t ->
            val s = t.trim()
            if (s.isEmpty()) 0 else s.toIntOrNull() ?: 0
        }
    }

    /** Returns null if the entire field is blank; otherwise a list where blank tokens => nulls. */
    private fun parseCsvNullable(input: String): List<Int?>? {
        val raw = input.trim()
        if (raw.isEmpty()) return null
        val tokens = raw.split(',', ';', ' ', '\n', '\t')
        return tokens.map { t ->
            val s = t.trim()
            if (s.isEmpty()) null else s.toIntOrNull()
        }
    }

    private fun emitError(msg: String) { viewModelScope.launch { _errors.emit(msg) } }
}

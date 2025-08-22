package com.example.memoryvisualizer.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryvisualizer.commands.AllocateCommand
import com.example.memoryvisualizer.commands.CommandManager
import com.example.memoryvisualizer.commands.CompactCommand
import com.example.memoryvisualizer.commands.ResetCommand
import com.example.memoryvisualizer.commands.Command
import com.example.memoryvisualizer.model.Scenario
import com.example.memoryvisualizer.model.Simulator
import com.example.memoryvisualizer.model.strategy.AllocationStrategy
import com.example.memoryvisualizer.model.strategy.FirstFitStrategy
import com.example.memoryvisualizer.model.strategy.BestFitStrategy
import com.example.memoryvisualizer.model.strategy.WorstFitStrategy
import com.example.memoryvisualizer.persistence.ScenarioManager
import com.example.memoryvisualizer.persistence.SimulationStorage
import com.example.memoryvisualizer.persistence.SimulationSnapshot
import com.example.memoryvisualizer.stub.SimulatorStub
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisualizerViewModel(
    context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_LAST_BLOCKS = "last_blocks"
        private const val KEY_LAST_PROCESSES = "last_processes"
        private const val KEY_LAST_STRATEGY = "last_strategy"
    }

    private val sim = SimulatorImpl()
    private val commandManager = CommandManager()
    private val storage = SimulationStorage(context)
    private val scenarioManager = ScenarioManager(context)

    private val _state = MutableStateFlow<AllocationResult?>(null)
    val state: StateFlow<AllocationResult?> = _state.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    private val _timelinePosition = MutableStateFlow(0)
    val timelinePosition: StateFlow<Int> = _timelinePosition.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    private val _savedSimulations = MutableStateFlow<List<String>>(emptyList())
    val savedSimulations: StateFlow<List<String>> = _savedSimulations.asStateFlow()

    private var lastInputBlocks: String = ""
    private var lastInputProcesses: String = ""

    private var blocks: List<Int> = emptyList()
    private var processes: List<Int> = emptyList()

    init {
        viewModelScope.launch {
            storage.listSimulations()
                .onSuccess { _savedSimulations.value = it }
                .onFailure { emitError("Failed to load saved simulations: ${it.message}") }
        }
        restoreState()
    }

    fun validateInput(blocksInput: String, processesInput: String) {
        try {
            val blocks = blocksInput.split(",").map { it.trim().toInt() }
            val processes = processesInput.split(",").map { it.trim().toInt() }
            
            if (blocks.isNotEmpty() && processes.isNotEmpty()) {
                this.blocks = blocks
                this.processes = processes
                lastInputBlocks = blocksInput
                lastInputProcesses = processesInput
                
                savedStateHandle[KEY_LAST_BLOCKS] = blocksInput
                savedStateHandle[KEY_LAST_PROCESSES] = processesInput
            }
        } catch (e: NumberFormatException) {
            viewModelScope.launch {
                _errors.emit("Invalid input: Please enter comma-separated numbers")
            }
        }
    }

    fun onLoad(blocksCsv: String, processesCsv: String) {
        lastInputBlocks = blocksCsv
        lastInputProcesses = processesCsv
        parseCsv(blocksCsv)?.let { b ->
            parseCsv(processesCsv)?.let { p ->
                val res = sim.loadWithStub(b, p)
                _state.value = res
                commandManager.clear() // Reset command history on new load
            } ?: emitError("Invalid processes list")
        } ?: emitError("Invalid blocks list")
    }

    fun onStrategySelected(name: String) {
        val strategy: AllocationStrategy = when(name) {
            "Best Fit" -> BestFitStrategy()
            "Worst Fit" -> WorstFitStrategy()
            "First Fit" -> FirstFitStrategy()
            else -> FirstFitStrategy()
        }
        sim.setStrategy(strategy)
        savedStateHandle[KEY_LAST_STRATEGY] = name
        
        if (blocks.isNotEmpty() && processes.isNotEmpty()) {
            onLoad(blocks.joinToString(","), processes.joinToString(","))
        }
    }

    fun onStep() {
        executeCommand(AllocateCommand(sim, sim.getCurrentStrategy()))
    }

    fun onCompact() {
        executeCommand(CompactCommand(sim))
    }

    fun onReset() {
        executeCommand(ResetCommand(sim))
    }

    fun onUndo() {
        if (commandManager.canUndo()) {
            commandManager.undo()
            update(sim.current())
        }
    }

    fun onRedo() {
        if (commandManager.canRedo()) {
            commandManager.redo()
            update(sim.current())
        }
    }

    fun onRun() {
        // Run all is a composite command made up of multiple step commands
        while (sim.hasWaitingProcesses()) {
            onStep()
        }
    }

    private fun executeCommand(command: Command) {
        if (commandManager.execute(command)) {
            update(sim.current())
        } else {
            emitError("Command execution failed")
        }
    }

    private fun parseCsv(csv: String): List<Int>? = try {
        csv.split(',', ';', ' ', '\n', '\t')
            .mapNotNull { token ->
                val t = token.trim()
                if (t.isEmpty()) null else t.toIntOrNull()?.takeIf { it > 0 }
            }
            .takeIf { it.isNotEmpty() }
    } catch (e: Exception) { null }

    private fun update(result: SimulatorStub.AllocationResultStub?) {
        if (result != null) {
            _state.value = result
            updateTimelineState()
        }
    }

    private fun updateTimelineState() {
        _timelinePosition.value = commandManager.position
        _totalSteps.value = commandManager.commandCount
    }

    private fun emitError(message: String) {
        viewModelScope.launch { _errors.emit(message) }
    }

    fun saveSimulation(name: String) {
        viewModelScope.launch {
            val current = _state.value ?: run {
                emitError("No simulation state to save")
                return@launch
            }

            storage.saveSimulation(name, SimulationSnapshot.from(current))
                .onSuccess {
                    storage.listSimulations()
                        .onSuccess { _savedSimulations.value = it }
                }
                .onFailure { emitError("Failed to save simulation: ${it.message}") }
        }
    }

    fun loadSimulation(name: String) {
        viewModelScope.launch {
            storage.loadSimulation(name)
                .onSuccess { state ->
                    onLoad(
                        state.blockSizes.joinToString(","),
                        state.processSizes.joinToString(",")
                    )
                    savedStateHandle[KEY_LAST_BLOCKS] = state.blockSizes.joinToString(",")
                    savedStateHandle[KEY_LAST_PROCESSES] = state.processSizes.joinToString(",")
                }
                .onFailure { emitError("Failed to load simulation: ${it.message}") }
        }
    }

    fun deleteSimulation(name: String) {
        viewModelScope.launch {
            storage.deleteSimulation(name)
                .onSuccess {
                    storage.listSimulations()
                        .onSuccess { _savedSimulations.value = it }
                }
                .onFailure { emitError("Failed to delete simulation: ${it.message}") }
        }
    }

    fun saveCurrentAsScenario(name: String, description: String) {
        val blocks = lastInputBlocks.split(',').mapNotNull { it.trim().toIntOrNull() }
        val processes = lastInputProcesses.split(',').mapNotNull { it.trim().toIntOrNull() }
        
        if (blocks.isEmpty() || processes.isEmpty()) {
            emitError("No valid blocks or processes to save")
            return
        }
        
        val scenario = Scenario(
            name = name,
            description = description,
            blocks = blocks,
            processes = processes
        )
        
        viewModelScope.launch {
            scenarioManager.saveScenario(scenario)
                .onFailure { emitError("Failed to save scenario: ${it.message}") }
        }
    }
    
    fun loadScenario(name: String) {
        scenarioManager.getScenario(name)?.let { scenario ->
            onLoad(
                scenario.blocks.joinToString(","),
                scenario.processes.joinToString(",")
            )
            // Save to state handle
            savedStateHandle[KEY_LAST_BLOCKS] = scenario.blocks.joinToString(",")
            savedStateHandle[KEY_LAST_PROCESSES] = scenario.processes.joinToString(",")
        } ?: emitError("Scenario not found: $name")
    }
    
    fun deleteScenario(name: String) {
        viewModelScope.launch {
            scenarioManager.deleteScenario(name)
                .onFailure { emitError("Failed to delete scenario: ${it.message}") }
        }
    }
    
    // SavedStateHandle implementation
    private fun restoreState() {
        savedStateHandle.get<String>(KEY_LAST_BLOCKS)?.let { blocks ->
            savedStateHandle.get<String>(KEY_LAST_PROCESSES)?.let { processes ->
                onLoad(blocks, processes)
            }
        }
        
        savedStateHandle.get<String>(KEY_LAST_STRATEGY)?.let { strategy ->
            onStrategySelected(strategy)
        }
    }
    
    init {
        restoreState()
    }
}

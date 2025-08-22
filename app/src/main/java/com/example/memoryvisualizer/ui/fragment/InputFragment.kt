package com.example.memoryvisualizer.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.dialog.LoadScenarioDialog
import com.example.memoryvisualizer.ui.dialog.SaveSimulationDialog
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InputFragment : Fragment() {
    private val vm: VisualizerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val blocksInput = view.findViewById<EditText>(R.id.input_blocks)
        val processesInput = view.findViewById<EditText>(R.id.input_processes)
        val algorithmSpinner = view.findViewById<Spinner>(R.id.spinner_algorithm)
        val errorText = view.findViewById<TextView>(R.id.text_error)
        val allocateButton = view.findViewById<Button>(R.id.button_allocate)
        val compactButton = view.findViewById<Button>(R.id.button_compact)
        val resetButton = view.findViewById<Button>(R.id.button_reset)
        val saveButton = view.findViewById<Button>(R.id.button_save)
        val loadButton = view.findViewById<Button>(R.id.button_load)

        setupAlgorithmSpinner(algorithmSpinner)
        setupInputValidation(blocksInput, processesInput)
        setupButtons(allocateButton, compactButton, resetButton, saveButton, loadButton)
        observeViewModelState(errorText)
    }

    private fun setupAlgorithmSpinner(spinner: Spinner?) {
        spinner?.let {
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.allocation_algorithms,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                it.adapter = adapter
            }

            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val strategy = parent?.getItemAtPosition(position)?.toString() ?: "First Fit"
                    vm.onStrategySelected(strategy)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupInputValidation(blocksInput: EditText?, processesInput: EditText?) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                vm.validateInput(
                    blocksInput?.text?.toString() ?: "",
                    processesInput?.text?.toString() ?: ""
                )
            }
        }

        blocksInput?.addTextChangedListener(textWatcher)
        processesInput?.addTextChangedListener(textWatcher)
    }

    private fun setupButtons(
        allocateButton: Button?,
        compactButton: Button?,
        resetButton: Button?,
        saveButton: Button?,
        loadButton: Button?
    ) {
        allocateButton?.setOnClickListener {
            vm.onStep()
        }

        compactButton?.setOnClickListener {
            vm.onCompact()
        }

        resetButton?.setOnClickListener {
            vm.onReset()
        }

        saveButton?.setOnClickListener {
            SaveSimulationDialog().show(childFragmentManager, "save_dialog")
        }

        loadButton?.setOnClickListener {
            LoadScenarioDialog().show(childFragmentManager, "load_dialog")
        }
    }
    
    private fun observeViewModelState(errorText: TextView?) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.errors.collect { error ->
                errorText?.let { text ->
                    text.text = error
                    text.visibility = if (error.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }
}

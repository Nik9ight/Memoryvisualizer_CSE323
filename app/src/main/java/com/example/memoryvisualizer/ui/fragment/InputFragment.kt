package com.example.memoryvisualizer.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InputFragment : Fragment() {

    private val vm: VisualizerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val blocksInput: EditText = view.findViewById(R.id.input_blocks)
        val processesInput: EditText = view.findViewById(R.id.input_processes)
        val loadBtn: Button = view.findViewById(R.id.btn_load)
        val strategySpinner: Spinner = view.findViewById(R.id.spinner_strategy)
        val errorText: TextView = view.findViewById(R.id.text_error)

        strategySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("First Fit", "Best Fit", "Worst Fit")
        )

        loadBtn.setOnClickListener {
            vm.onLoad(blocksInput.text.toString(), processesInput.text.toString())
        }

        strategySpinner.setOnItemSelectedListenerCompat { pos ->
            vm.onStrategySelected(strategySpinner.getItemAtPosition(pos).toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.errors.collectLatest { msg ->
                errorText.text = msg
                errorText.visibility = if (msg.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun Spinner.setOnItemSelectedListenerCompat(onSelected: (Int) -> Unit) {
        onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelected(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }
}

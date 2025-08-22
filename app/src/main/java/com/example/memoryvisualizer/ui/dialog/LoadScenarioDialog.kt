package com.example.memoryvisualizer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.util.UiMessageHelper
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoadScenarioDialog : DialogFragment() {
    private val viewModel: VisualizerViewModel by activityViewModels()
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val scenarios = viewModel.savedSimulations.value
        
        return if (scenarios.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.load_simulation)
                .setMessage(R.string.msg_no_scenarios)
                .setPositiveButton(R.string.ok, null)
                .create()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.load_simulation)
                .setItems(scenarios.toTypedArray()) { _, position ->
                    viewModel.loadScenario(scenarios[position])
                    UiMessageHelper.showToast(requireContext(), R.string.success_load)
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        }
    }
}

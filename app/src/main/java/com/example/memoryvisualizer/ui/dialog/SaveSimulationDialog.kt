package com.example.memoryvisualizer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.util.UiMessageHelper
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import com.google.android.material.textfield.TextInputLayout

class SaveSimulationDialog : DialogFragment() {
    private val viewModel: VisualizerViewModel by activityViewModels()
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_save_simulation, null)
        
        val nameInput = view.findViewById<TextInputLayout>(R.id.input_name)
        val descriptionInput = view.findViewById<TextInputLayout>(R.id.input_description)
        
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.save_simulation)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.editText?.text?.toString()?.trim()
                val description = descriptionInput.editText?.text?.toString()?.trim()
                
                when {
                    name.isNullOrEmpty() -> {
                        UiMessageHelper.showToast(requireContext(), R.string.error_empty_name)
                    }
                    else -> {
                        viewModel.saveCurrentAsScenario(name, description ?: "")
                        UiMessageHelper.showToast(requireContext(), R.string.success_save)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}

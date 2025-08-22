package com.example.memoryvisualizer.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InputFragment : Fragment() {
    private val vm: VisualizerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilBlocks: TextInputLayout = view.findViewById(R.id.til_blocks)
        val tilProcesses: TextInputLayout = view.findViewById(R.id.til_processes)
        val blocksInput: EditText = view.findViewById(R.id.input_blocks)
        val processesInput: EditText = view.findViewById(R.id.input_processes)
        val loadBtn: Button = view.findViewById(R.id.btn_load)
        val strategyDropdown: MaterialAutoCompleteTextView = view.findViewById(R.id.spinner_strategy)
        val errorCard: View = view.findViewById(R.id.card_error)
        val errorText: TextView = view.findViewById(R.id.text_error)

        // Raise Load button above the keyboard (IME) by adjusting its bottom margin dynamically
        val baseBottomMargin = resources.getDimensionPixelSize(R.dimen.section_gap)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            loadBtn.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = baseBottomMargin + imeHeight
            }
            insets
        }

        val strategies = listOf("First Fit", "Best Fit", "Worst Fit")
        strategyDropdown.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, strategies))
        if (strategyDropdown.text.isNullOrBlank()) strategyDropdown.setText(strategies.first(), false)
        vm.onStrategySelected(strategyDropdown.text.toString())

        strategyDropdown.setOnItemClickListener { _, _, position, _ ->
            val name = strategies[position]
            strategyDropdown.setText(name, false)
            vm.onStrategySelected(name)
            Snackbar.make(view, getString(R.string.fmt_strategy_selected, name), Snackbar.LENGTH_SHORT).show()
        }

        fun validateNow(): Boolean {
            tilBlocks.error = null
            tilProcesses.error = null
            val bOk = parseCsv(blocksInput.text.toString()) != null
            val pOk = parseCsv(processesInput.text.toString()) != null
            if (!bOk) tilBlocks.error = getString(R.string.error_invalid_blocks)
            if (!pOk) tilProcesses.error = getString(R.string.error_invalid_processes)
            return bOk && pOk
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadBtn.isEnabled = blocksInput.text.isNotBlank() && processesInput.text.isNotBlank()
                tilBlocks.error = null
                tilProcesses.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        blocksInput.addTextChangedListener(watcher)
        processesInput.addTextChangedListener(watcher)

        loadBtn.isEnabled = false
        loadBtn.setOnClickListener {
            if (validateNow()) {
                vm.onLoad(blocksInput.text.toString(), processesInput.text.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.errors.collectLatest { msg ->
                errorText.text = msg
                if (msg.isEmpty()) {
                    errorCard.visibility = View.GONE
                } else {
                    errorCard.visibility = View.VISIBLE
                    errorCard.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom))
                }
            }
        }
    }

    private fun parseCsv(csv: String): List<Int>? = try {
        csv.split(',', ';', ' ', '\n', '\t')
            .mapNotNull { token ->
                val t = token.trim()
                if (t.isEmpty()) null else t.toIntOrNull()?.takeIf { it > 0 }
            }
            .takeIf { it.isNotEmpty() }
    } catch (_: Exception) { null }
}

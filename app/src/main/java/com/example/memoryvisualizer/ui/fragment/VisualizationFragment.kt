package com.example.memoryvisualizer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.stub.SimulatorStub
import com.example.memoryvisualizer.ui.view.MemoryCanvasView
import com.example.memoryvisualizer.ui.view.RenderBlockMapper
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VisualizationFragment : Fragment() {

    private val vm: VisualizerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_visualization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val canvas = view.findViewById<MemoryCanvasView>(R.id.memory_canvas)
        val actionText = view.findViewById<TextView>(R.id.text_action)
        val statsTextView = view.findViewById<TextView>(R.id.text_stats)

        // Redundant observer previously here has been removed.

        // Call setupViews only if all required views are found,
        // as setupViews and updateVisualization expect non-null views.
        if (canvas != null && actionText != null && statsTextView != null) {
            setupViews(view, canvas, actionText, statsTextView)
        } else {
            // Optionally, log an error or handle the case where one or more views are missing.
            // For example: Log.e("VisualizationFragment", "One or more essential views are not found in the layout.")
        }
    }

    private fun setupViews(
        view: View,
        canvas: MemoryCanvasView,
        actionText: TextView,
        statsText: TextView
    ) {
        // Setup button click listeners
        view.findViewById<Button>(R.id.btn_step)?.setOnClickListener { vm.onStep() }
        view.findViewById<Button>(R.id.btn_run)?.setOnClickListener { vm.onRun() }
        view.findViewById<Button>(R.id.btn_compact)?.setOnClickListener { vm.onCompact() }
        view.findViewById<Button>(R.id.btn_reset)?.setOnClickListener { vm.onReset() }
        view.findViewById<Button>(R.id.btn_undo)?.setOnClickListener { vm.onUndo() }
        view.findViewById<Button>(R.id.btn_redo)?.setOnClickListener { vm.onRedo() }

        // Observe state changes
        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { res: SimulatorStub.AllocationResultStub? ->
                res?.let { result ->
                    updateVisualization(
                        canvas = canvas,
                        actionText = actionText,
                        statsText = statsText,
                        result = result
                    )
                }
            }
        }
    }

    private fun updateVisualization(
        canvas: MemoryCanvasView,
        actionText: TextView,
        statsText: TextView,
        result: SimulatorStub.AllocationResultStub
    ) {
        canvas.submit(RenderBlockMapper.map(result.blocks))
        actionText.text = result.action
        statsText.text = getString(
            R.string.fmt_stats,
            result.stats.internalTotal,
            result.stats.externalFree,
            result.stats.largestFree,
            result.stats.holeCount,
            String.format("%.1f", result.stats.successPct)
        )
    }
}

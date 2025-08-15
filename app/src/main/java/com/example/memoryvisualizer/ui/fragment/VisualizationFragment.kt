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
        val canvas: MemoryCanvasView = view.findViewById(R.id.memory_canvas)
        val actionText: TextView = view.findViewById(R.id.text_action)
        val statsText: TextView = view.findViewById(R.id.text_stats)

        view.findViewById<Button>(R.id.btn_step).setOnClickListener { vm.onStep() }
        view.findViewById<Button>(R.id.btn_run).setOnClickListener { vm.onRun() }
        view.findViewById<Button>(R.id.btn_compact).setOnClickListener { vm.onCompact() }
        view.findViewById<Button>(R.id.btn_reset).setOnClickListener { vm.onReset() }
        view.findViewById<Button>(R.id.btn_undo).setOnClickListener { vm.onUndo() }
        view.findViewById<Button>(R.id.btn_redo).setOnClickListener { vm.onRedo() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { res: SimulatorStub.AllocationResultStub? ->
                if (res != null) {
                    canvas.submit(RenderBlockMapper.map(res.blocks))
                    actionText.text = res.action
                    statsText.text = getString(
                        R.string.fmt_stats,
                        res.stats.internalTotal,
                        res.stats.externalFree,
                        res.stats.largestFree,
                        res.stats.holeCount,
                        String.format("%.1f", res.stats.successPct)
                    )
                }
            }
        }
    }
}

package com.example.memoryvisualizer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VisualizationFragment : Fragment() {
    private val vm: VisualizerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_visualization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val canvas: MemoryCanvasView = view.findViewById(R.id.memory_canvas)
        val actionText: TextView = view.findViewById(R.id.text_action)
        val statsText: TextView = view.findViewById(R.id.text_stats)
        val emptyOverlay: View = view.findViewById(R.id.empty_state_overlay)

        val btnStep: Button = view.findViewById(R.id.btn_step)
        val btnRun: Button = view.findViewById(R.id.btn_run)
        val btnCompact: Button = view.findViewById(R.id.btn_compact)
        val btnReset: Button = view.findViewById(R.id.btn_reset)
        val btnUndo: Button = view.findViewById(R.id.btn_undo)
        val btnRedo: Button = view.findViewById(R.id.btn_redo)

        btnStep.contentDescription = getString(R.string.action_step)
        btnRun.contentDescription = getString(R.string.action_run)
        btnCompact.contentDescription = getString(R.string.action_compact)
        btnReset.contentDescription = getString(R.string.action_reset)
        btnUndo.contentDescription = getString(R.string.action_undo)
        btnRedo.contentDescription = getString(R.string.action_redo)

        btnStep.tooltipText = getString(R.string.action_step)
        btnRun.tooltipText = getString(R.string.action_run)
        btnCompact.tooltipText = getString(R.string.action_compact)
        btnReset.tooltipText = getString(R.string.action_reset)
        btnUndo.tooltipText = getString(R.string.action_undo)
        btnRedo.tooltipText = getString(R.string.action_redo)

        fun attachPressAnim(b: View) {
            val press = AnimationUtils.loadAnimation(requireContext(), R.anim.button_press)
            b.setOnTouchListener { v, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> v.startAnimation(press)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.clearAnimation()
                }
                false
            }
        }
        listOf(btnStep, btnRun, btnCompact, btnReset, btnUndo, btnRedo).forEach { attachPressAnim(it) }

        btnStep.setOnClickListener { vm.onStep() }
        btnRun.setOnClickListener { vm.onRun() }
        btnCompact.setOnClickListener { vm.onCompact() }
        btnReset.setOnClickListener { vm.onReset() }
        btnUndo.setOnClickListener { vm.onUndo() }
        btnRedo.setOnClickListener { vm.onRedo() }

        canvas.onBlockClick = { b: RenderBlockMapper.RenderBlock ->
            if (b.isFree) {
                Snackbar.make(view, getString(R.string.fmt_block_info_free, b.size), Snackbar.LENGTH_SHORT).show()
            } else {
                val pid = b.processId ?: "?"
                Snackbar.make(view, getString(R.string.fmt_block_info_alloc, pid, b.size), Snackbar.LENGTH_SHORT).show()
            }
        }
        canvas.onBlockLongPress = { b: RenderBlockMapper.RenderBlock ->
            showBlockInfoSheet(b)
        }

        // Initial state: disable controls until data is loaded
        setControlsEnabled(listOf(btnStep, btnRun, btnCompact, btnReset), false)
        btnUndo.isEnabled = false
        btnRedo.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { res: SimulatorStub.AllocationResultStub? ->
                if (res != null) {
                    emptyOverlay.visibility = View.GONE
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
                    setControlsEnabled(listOf(btnStep, btnRun, btnCompact, btnReset), true)
                    btnUndo.isEnabled = vm.canUndo()
                    btnRedo.isEnabled = vm.canRedo()
                } else {
                    emptyOverlay.visibility = View.VISIBLE
                    setControlsEnabled(listOf(btnStep, btnRun, btnCompact, btnReset), false)
                    btnUndo.isEnabled = false
                    btnRedo.isEnabled = false
                }
            }
        }
    }

    private fun showBlockInfoSheet(b: RenderBlockMapper.RenderBlock) {
        val ctx = requireContext()
        val dialog = BottomSheetDialog(ctx)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_block_info, null)
        val title: TextView = sheet.findViewById(R.id.title)
        val subtitle: TextView = sheet.findViewById(R.id.subtitle)
        val close: Button = sheet.findViewById(R.id.btn_close)
        if (b.isFree) {
            title.text = getString(R.string.fmt_block_info_free, b.size)
            subtitle.text = ctx.getString(R.string.label_stats)
        } else {
            title.text = getString(R.string.fmt_block_info_alloc, b.processId ?: "?", b.size)
            subtitle.text = ctx.getString(R.string.label_last_action)
        }
        close.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(sheet)
        dialog.show()
    }

    private fun setControlsEnabled(buttons: List<Button>, enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
    }
}

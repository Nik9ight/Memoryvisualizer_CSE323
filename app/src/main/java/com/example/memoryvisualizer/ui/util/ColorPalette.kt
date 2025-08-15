package com.example.memoryvisualizer.ui.util

import android.graphics.Color
import kotlin.math.abs
import kotlin.random.Random

object ColorPalette {
    // Color-blind friendly distinct palette (no red/green confusion)
    private val base = listOf(
        0xFF1B9E77.toInt(), // teal
        0xFFD95F02.toInt(), // orange
        0xFF7570B3.toInt(), // purple
        0xFFE7298A.toInt(), // magenta
        0xFF66A61E.toInt(), // olive
        0xFFE6AB02.toInt(), // mustard
        0xFFA6761D.toInt(), // brown
        0xFF666666.toInt()  // gray
    )

    private val cache = mutableMapOf<String, Int>()

    fun colorForProcess(pid: String?): Int {
        if (pid == null) return Color.LTGRAY
        return cache.getOrPut(pid) {
            val idx = abs(pid.hashCode()) % base.size
            base[idx]
        }
    }
}

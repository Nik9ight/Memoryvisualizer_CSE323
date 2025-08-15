package com.example.memoryvisualizer.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.fragment.InputFragment
import com.example.memoryvisualizer.ui.fragment.VisualizationFragment
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel

class MainActivity : AppCompatActivity() {
    private val vm: VisualizerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container_input, InputFragment())
                replace(R.id.container_visualization, VisualizationFragment())
            }
        }
    }
}

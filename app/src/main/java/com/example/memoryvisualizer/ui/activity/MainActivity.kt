package com.example.memoryvisualizer.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.memoryvisualizer.R
import com.example.memoryvisualizer.ui.fragment.InputFragment
import com.example.memoryvisualizer.ui.fragment.VisualizationFragment
import com.example.memoryvisualizer.ui.viewmodel.VisualizerViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val vm: VisualizerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Phone layout: ViewPager2 + TabLayout
        val viewPager = findViewById<ViewPager2?>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout?>(R.id.tab_layout)
        if (viewPager != null && tabLayout != null) {
            if (viewPager.adapter == null) {
                viewPager.adapter = MainPagerAdapter(this)
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = when (position) {
                        0 -> getString(R.string.tab_title_setup)
                        else -> getString(R.string.tab_title_visualize)
                    }
                }.attach()
            }
            // Navigate to Visualize after a successful load
            lifecycleScope.launch {
                vm.loaded.collectLatest {
                    viewPager.currentItem = 1
                }
            }
            return
        }

        // Tablet/two-pane layout fallback
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container_input, InputFragment())
                replace(R.id.container_visualization, VisualizationFragment())
            }
        }
    }
}

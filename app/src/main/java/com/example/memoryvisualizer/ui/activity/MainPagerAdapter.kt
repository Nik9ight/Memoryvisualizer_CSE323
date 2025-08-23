package com.example.memoryvisualizer.ui.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.memoryvisualizer.ui.fragment.InputFragment
import com.example.memoryvisualizer.ui.fragment.VisualizationFragment

class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> InputFragment()
        else -> VisualizationFragment()
    }
}


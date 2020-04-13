package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.isanechek.elitawallpaperx._layout
import kotlinx.android.synthetic.main.main_fragment_layout.*


class MainFragment : Fragment(_layout.main_fragment_layout) {

    private val vm: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mf_toolbar.apply {
            title = "WallpapersX"
        }

        val pagerAdapter = MainPagerAdapter()
        with(mf_pager) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = pagerAdapter
        }

        vm.data.observe(viewLifecycleOwner, Observer { data ->
            pagerAdapter.submit(data)
        })

    }

    override fun onResume() {
        super.onResume()
        vm.loadWallpapers()
    }
}
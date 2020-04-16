package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isanechek.elitawallpaperx.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_fragment_layout.*


class MainFragment : Fragment(_layout.main_fragment_layout) {

    private val vm: MainViewModel by viewModels()
    private val pagerAdapter by lazy { MainPagerAdapter() }
    private val mainAdapter by lazy { MainWallpapersAdapter() }
    private val pagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            mf_toolbar_counter.text = String.format("%d/%d", position.plus(1), pagerAdapter.itemCount)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mf_toolbar.hideCustomLayout()
        mf_toolbar_title.text = "WallpaperX"

        // pager
        with(mf_pager) {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            adapter = pagerAdapter
            registerOnPageChangeCallback(pagerListener)
        }

        vm.data.observe(viewLifecycleOwner, Observer { data ->
            pagerAdapter.submit(data)
            mainAdapter.submit(data)
        })
        pagerAdapter.setOnListenerCallback(object : MainPagerAdapter.ListenerCallback {
            override fun onItemClick(data: String, position: Int) {
                findNavController().navigate(_id.main_go_detail_fragment, bundleOf("path" to data))
            }
        })

        mf_fab.onClick {
            showDialog()
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            customView(viewRes = _layout.wallpaper_dialog_layout)
            lifecycleOwner(this@MainFragment)
        }.onShow {
            val list = it.getCustomView().findViewById<RecyclerView>(_id.dialog_list)
            with(list) {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = mainAdapter
            }
            mainAdapter.setClickListener(object : MainWallpapersAdapter.ClickListener {
                override fun onItemClick(position: Int) {
                    it.dismiss()
                    mf_pager.currentItem = position
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        vm.loadWallpapers()
    }

    override fun onPause() {
        mf_pager.unregisterOnPageChangeCallback(pagerListener)
        super.onPause()
    }
}
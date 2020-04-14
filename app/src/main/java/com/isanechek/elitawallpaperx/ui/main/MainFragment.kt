package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isanechek.elitawallpaperx._id
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_fragment_layout.*


class MainFragment : Fragment(_layout.main_fragment_layout) {

    private var positionState = 0
    private val vm: MainViewModel by viewModels()
    private val mainAdapter by lazy { MainWallpapersAdapter() }
    private val pagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mf_toolbar.apply {
            title = ""
            setBackOrCloseButton {
                mf_container.transitionToStart()
                d { "load pos $positionState" }
                mf_pager.currentItem = positionState
            }
        }

        // pager
        val pagerAdapter = MainPagerAdapter()
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
                Picasso.get().load(data).into(mf_detail_iv)
                positionState = position
                if (pagerAdapter.itemCount != 0) {
                    positionState.plus(1)
                }
                d { "save pos $positionState" }
                mf_container.transitionToEnd()
            }
        })

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
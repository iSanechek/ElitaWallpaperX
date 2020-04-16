package com.isanechek.elitawallpaperx.ui.main

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.isanechek.elitawallpaperx.*
import com.isanechek.elitawallpaperx.models.NewInfo
import com.isanechek.elitawallpaperx.ui.base.bind
import kotlinx.android.synthetic.main.main_fragment_layout.*
import kotlinx.android.synthetic.main.what_is_new_item_layout.view.*


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
            d { "pos $position" }
            mf_toolbar_counter.text =
                String.format("%d/%d", position.plus(1), pagerAdapter.itemCount)
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

        mf_menu_btn.onClick {
            showSettingsDialog()
        }

        mf_company_tv.onClick {
            showAboutDialog()
        }

        mf_toolbar_counter.onClick {
            // тут надо заимплементить посхалку
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            customView(viewRes = _layout.base_list_dialog_layout)
            lifecycleOwner(this@MainFragment)
            positiveButton(text = "close") {
                it.dismiss()
            }
        }.onShow {
            mainAdapter.setClickListener(object : MainWallpapersAdapter.ClickListener {
                override fun onItemClick(position: Int) {
                    it.dismiss()
                    it.onDismiss {
                        mf_pager.currentItem = position
                    }
                }
            })

            with(it.findViewById<RecyclerView>(_id.dialog_list)) {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = mainAdapter
            }
        }
    }

    private fun showSettingsDialog() {
        val items = listOf<CharSequence>("No wallpaper", "What is new", "About as")
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            lifecycleOwner(this@MainFragment)
            listItems(items = items) { d, i, _ ->
                d.dismiss()
                when (i) {
                    0 -> showNoWallpaperDialog()
                    1 -> showWhatNewDialog()
                    2 -> showAboutDialog()
                    else -> Unit
                }
            }
            negativeButton(text = "Close") {
                it.dismiss()
            }
        }
    }

    private fun showNoWallpaperDialog() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "No wallpaper")
            message(res = _string.no_wallpapers_message)
            positiveButton(text = "remove") {
                it.dismiss()
            }
            negativeButton(text = "close") {
                it.dismiss()
            }
        }
    }

    private fun showAboutDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            lifecycleOwner(this@MainFragment)
            customView(viewRes = _layout.about_dialog_layout)
            setPeekHeight(literal = 1000)
            negativeButton(text = "ok") {
                it.dismiss()
            }
        }
    }

    private fun showWhatNewDialog() {
        val testDescription = mutableListOf("Fix base lase", "Improve speed performance", "Add some bags")
        val testData = mutableListOf(
            NewInfo(version = "10.0.0", description = testDescription, date = "16.04.2020"),
            NewInfo(version = "9.18.44", description = testDescription, date = "08.01.2020"),
            NewInfo(version = "9.12.55", description = testDescription, date = "25.11.2019"),
            NewInfo(version = "9.12.55", description = testDescription, date = "18.09.2019"),
            NewInfo(version = "9.12.55", description = testDescription, date = "12.07.2019")
        )
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "What is new")
            lifecycleOwner(this@MainFragment)
            customView(viewRes = _layout.base_list_dialog_layout)
            positiveButton(text = "close") {
                it.dismiss()
            }
        }.onShow {
            with(it.findViewById<RecyclerView>(_id.dialog_list)) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                this.bind(testData, _layout.what_is_new_item_layout) { item: NewInfo ->
                    wni_title_tv.text = item.version
                    wni_date_tv.text = item.date
                    val sb = StringBuilder()
                    val d = item.description
                    d.forEachIndexed { index, s ->
                        sb.append(s)
                        if (d.size.minus(1) != index) {
                            sb.append("\n")
                        }
                    }

                    wni_description_tv.text = sb.toString()
                }
            }
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
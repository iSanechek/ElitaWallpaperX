package com.isanechek.elitawallpaperx.ui.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.inflate
import com.isanechek.elitawallpaperx.onClick
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.main_pager_item_layout.*

class MainPagerAdapter : RecyclerView.Adapter<MainPagerAdapter.PagerHolder>() {

    private var listener: ListenerCallback? = null

    inner class PagerHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: String, position: Int, callback: ListenerCallback?) {
            mpi_background_iv.load(data)
            mpi_background_iv.onClick { callback?.onItemClick(data, position) }
        }
    }

    private val paths = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder =
        PagerHolder(parent.inflate(_layout.main_pager_item_layout))

    override fun getItemCount(): Int = paths.size

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        val path = paths[position]
        holder.bind(path, position, listener)
    }

    fun submit(data: List<String>) {
        if (paths.isNotEmpty()) paths.clear()
        paths.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnListenerCallback(callback: ListenerCallback) {
        this.listener = callback
    }

    interface ListenerCallback {
        fun onItemClick(data: String, position: Int)
    }

}
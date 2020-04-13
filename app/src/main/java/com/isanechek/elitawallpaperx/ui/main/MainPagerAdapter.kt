package com.isanechek.elitawallpaperx.ui.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.d
import com.isanechek.elitawallpaperx.inflate
import com.isanechek.elitawallpaperx.ui.base.BaseViewHolder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.main_pager_item_layout.*

class MainPagerAdapter : RecyclerView.Adapter<MainPagerAdapter.PagerHolder>() {

    inner class PagerHolder(itemView: View) : BaseViewHolder<String>(itemView) {

        override fun bind(data: String) {
            Picasso.get().load(data).into(mpi_iv)
        }
    }

    private val paths = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder =
        PagerHolder(parent.inflate(_layout.main_pager_item_layout))

    override fun getItemCount(): Int = paths.size

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        holder.bind(paths[position])
    }

    fun submit(data: List<String>) {
        if (paths.isNotEmpty()) paths.clear()
        paths.addAll(data)
        notifyDataSetChanged()
    }
}
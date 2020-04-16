package com.isanechek.elitawallpaperx.ui.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.inflate
import com.isanechek.elitawallpaperx.models.NewInfo
import kotlinx.android.extensions.LayoutContainer

class WhatIsNewAdapter :  RecyclerView.Adapter<WhatIsNewAdapter.WhatNewHolder>() {

    inner class WhatNewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: NewInfo, callback: () -> Unit) {

        }
    }

    private val items = mutableListOf<NewInfo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatNewHolder =
        WhatNewHolder(parent.inflate(_layout.what_is_new_item_layout))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WhatNewHolder, position: Int) {
        holder.bind(items[position], callback = {})
    }

    fun submit(data: List<NewInfo>) {
//        if (items.)
    }
}
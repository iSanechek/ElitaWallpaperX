package com.isanechek.elitawallpaperx.ui.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isanechek.elitawallpaperx._layout
import com.isanechek.elitawallpaperx.inflate
import com.isanechek.elitawallpaperx.models.CarouselItemTag
import com.isanechek.elitawallpaperx.onClick
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.main_list_item_layout.*

class MainWallpapersAdapter :
    RecyclerView.Adapter<MainWallpapersAdapter.MainListHolder>() {

    private val picasso: Picasso = Picasso.get()
    private var listener: ClickListener? = null

    inner class MainListHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(data: String, position: Int, picasso: Picasso, callback: ClickListener?) {
            mli_container.onClick { callback?.onItemClick(position) }
            picasso.load(data)
                .fit()
                .centerCrop()
                .into(mli_iv)
            itemView.rootView.tag =
                CarouselItemTag(
                    id = data.replaceBefore("moto_", ""),
                    position = position
                )
        }
    }

    private val data = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainListHolder =
        MainListHolder(parent.inflate(_layout.main_list_item_layout))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: MainListHolder, position: Int) {
        holder.bind(data[position], position, picasso, listener)
    }

    fun submit(newData: List<String>) {
        if (data.isNotEmpty()) data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    fun setClickListener(callback: ClickListener?) {
        this.listener = callback
    }

    interface ClickListener {
        fun onItemClick(position: Int)
    }

}
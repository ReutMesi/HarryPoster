package com.example.harryposter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PictureAdapter(private val images: List<Int>) : RecyclerView.Adapter<PictureAdapter.ViewHolder>() {

    // ViewHolder inner class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageRes = images[position]
        holder.itemImage.setImageResource(imageRes)
    }

    override fun getItemCount(): Int = images.size
}

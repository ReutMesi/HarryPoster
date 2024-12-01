package com.example.harryposter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView

class PictureAdapter(private val images: List<Int>) : RecyclerView.Adapter<PictureAdapter.ViewHolder>() {

    // ViewHolder inner class
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
    }

    private val animatedPositions = HashSet<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageRes = images[position]

        // Set the image resource
        holder.itemImage.setImageResource(imageRes)

        // Set a dynamic content description (e.g., "Image at position X")
        holder.itemImage.contentDescription = "Image at position $position"

        // Avoid re-animating already visible items
        if (!animatedPositions.contains(position)) {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recyclerview_slide_in)
            holder.itemView.startAnimation(animation)
            animatedPositions.add(position)
        }
    }

    override fun getItemCount(): Int = images.size
}

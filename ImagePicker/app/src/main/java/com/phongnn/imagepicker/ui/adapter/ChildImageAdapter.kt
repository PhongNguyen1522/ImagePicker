package com.phongnn.imagepicker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.databinding.ImageItemBinding

class ChildImageAdapter(private val images: List<ByteArray>): RecyclerView.Adapter<ChildImageAdapter.ChildImageViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ChildImageViewHolder, position: Int) {
        val imgUrl = images[position]
        Glide.with(holder.binding.root)
            .load(imgUrl)
            .into(holder.binding.imvChildImage)
    }

    class ChildImageViewHolder(val binding: ImageItemBinding): RecyclerView.ViewHolder(binding.root)

}
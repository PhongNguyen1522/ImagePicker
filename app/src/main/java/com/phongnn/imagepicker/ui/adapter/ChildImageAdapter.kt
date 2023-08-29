package com.phongnn.imagepicker.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.databinding.ImageItemBinding

class ChildImageAdapter(
    private val newImages: List<MyImage>,
    private val savedImages: List<ImageEntity>,
    private var itemClickListener: ItemClickListener?,
) : RecyclerView.Adapter<ChildImageAdapter.ChildImageViewHolder>() {

    private var clickedPosition: Int = -1

    interface ItemClickListener {
        fun onDownloadImage(imageEntity: ImageEntity)
        fun onDownloadImageToStorage(myImage: MyImage)
        fun onShowSavedImage(imageEntity: ImageEntity)
        fun onShowDownloadedImage(myImage: MyImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildImageViewHolder, position: Int) {
        holder.bind(newImages[position], position)
    }

    override fun getItemCount(): Int {
        return newImages.size
    }

    inner class ChildImageViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(myImage: MyImage, position: Int) {

            // Show Images for api call
            Glide.with(binding.root)
                .load(myImage.matrix)
                .into(binding.imvChildImage)

            // Check isDownloaded to show or not
            for (img in savedImages) {
                val savedPosition = img.id
                if (position == savedPosition) {
                    binding.cvIcDownload.visibility = View.GONE
                }
            }

            binding.imvChildImage.setOnClickListener {
                // Logic for toggle item selected
                if (clickedPosition != position) {
                    notifyItemChanged(clickedPosition)
                    clickedPosition = position
                    notifyItemChanged(position)
                } else {
                    notifyItemChanged(position)
                }

                // cnt: counting the number of images that is in db
                var cnt = 0
                // Check that images 've already been database or not, if not, save it
                for (img in savedImages) {
                    val savedImagePosition = img.id
                    if (savedImagePosition == position) {
                        // Show Image on main Activity
                        itemClickListener?.onShowSavedImage(img)
                    } else {
                        cnt++
                    }
                }
                // Save new image
                if (cnt == savedImages.size) {
                    val imageEntity = ImageEntity(position, myImage.matrix, myImage.uri, myImage.type)
                    itemClickListener?.onDownloadImage(imageEntity)

                    itemClickListener?.onDownloadImageToStorage(myImage)
                    itemClickListener?.onShowDownloadedImage(myImage)
//                    itemClickListener?.onShowSavedImage(imageEntity)
                    binding.cvIcDownload.visibility = View.GONE
                    binding.imvDownloadedImage.visibility = View.VISIBLE
                    notifyItemChanged(position)
                }

            }
            // Show Check Icon
            if (clickedPosition == position) {
                if(binding.cvIcDownload.isVisible) {
                    binding.imvDownloadedImage.visibility = View.VISIBLE
                    binding.cvIcDownload.visibility = View.GONE
                } else {
                    binding.imvDownloadedImage.visibility = View.VISIBLE
                }
            } else {
                binding.imvDownloadedImage.visibility = View.GONE
            }

        }
    }

}
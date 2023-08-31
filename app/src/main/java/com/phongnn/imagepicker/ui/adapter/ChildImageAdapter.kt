package com.phongnn.imagepicker.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.data.model.ImageInfo
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.databinding.ImageItemBinding

class ChildImageAdapter(
    context: Context,
    private val newImages: List<MyImage>,
    private val imageInfoList: List<ImageInfo>,
    private var itemClickListener: ItemClickListener?,
) : RecyclerView.Adapter<ChildImageAdapter.ChildImageViewHolder>() {

    private var clickedPosition: Int = -1

    interface ItemClickListener {
        fun onDownloadImageToStorage(myImage: MyImage)
        fun onShowDownloadedImage(imageInfo: ImageInfo)
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

            // Check isDownloaded to show or not
            checkCurrentImageInStorage(
                myImage.imageName, binding.cvIcDownload)

            // Show Images for api call
            Glide.with(binding.root)
                .load(myImage.matrix)
                .into(binding.imvChildImage)

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
                // Check that images 've already been in folder or not, if not, save it
                for (imgInfo in imageInfoList) {
                    if (myImage.imageName.equals(imgInfo.imageName, false)) {
                        itemClickListener?.onShowDownloadedImage(imgInfo)
                    } else {
                        cnt++
                    }
                }
                // If there's any image in folder(cnt > 0), just show image
                if (cnt == imageInfoList.size) {
                    notifyItemChanged(position)
                    itemClickListener?.apply {
                        onDownloadImageToStorage(myImage)
                    }
                    for (img in imageInfoList) {
                        if (img.imageName.equals(myImage.imageName, false)) {
                            itemClickListener?.onShowDownloadedImage(img)
                        }
                    }
                }
            }
            // Show Check Icon
            if (clickedPosition == position) {
                binding.imvDownloadedImage.visibility = View.VISIBLE
            } else {
                binding.imvDownloadedImage.visibility = View.GONE
            }

        }
    }

    private fun checkCurrentImageInStorage(
        name: String,
        cvIcDownload: CardView
    ) {
        for (imgInfo in imageInfoList) {
            if (imgInfo.imageName.equals(name, false)) {
                cvIcDownload.visibility = View.GONE
            }
        }
    }

}
package com.phongnn.imagepicker.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.data.model.ImageInfo
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.data.utils.CommonConstant.END_VIEW_TYPE
import com.phongnn.imagepicker.data.utils.CommonConstant.NORMAL_VIEW_TYPE
import com.phongnn.imagepicker.data.utils.CommonConstant.START_VIEW_TYPE
import com.phongnn.imagepicker.databinding.ImageItemBinding
import com.phongnn.imagepicker.databinding.ImageItemEndBinding
import com.phongnn.imagepicker.databinding.ImageItemStartBinding

class ChildImageAdapter(
    context: Context,
    private val newImages: List<MyImage>,
    private val imageInfoList: List<ImageInfo>,
    private var itemClickListener: ItemClickListener?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var clickedPosition: Int = -1

    interface ItemClickListener {
        fun onDownloadImageToStorage(myImage: MyImage, position: Int)
        fun onShowDownloadedImage(imageInfo: ImageInfo)
        fun onClickButtonNone()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            START_VIEW_TYPE -> {
                val binding = ImageItemStartBinding.inflate(inflater, parent, false)
                StartChildImageViewHolder(binding)
            }
            NORMAL_VIEW_TYPE -> {
                val binding = ImageItemBinding.inflate(inflater, parent, false)
                ChildImageViewHolder(binding)
            }
            END_VIEW_TYPE -> {
                val binding = ImageItemEndBinding.inflate(inflater, parent, false)
                return EndChildImageViewHolder(binding)
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val image = newImages[position]

        when (holder.itemViewType) {
            START_VIEW_TYPE -> {
                val viewHolder = holder as StartChildImageViewHolder
                viewHolder.bind(image, position)
            }
            NORMAL_VIEW_TYPE -> {
                val viewHolder = holder as ChildImageViewHolder
                if (position == 0) {
                    viewHolder.bindFirstImage(R.drawable.btn_none, position)
                } else {
                    viewHolder.bind(image, position)
                }
            }
            END_VIEW_TYPE -> {
                val viewHolder = holder as EndChildImageViewHolder
                viewHolder.bind(image, position)
            }
        }


    }

    override fun getItemCount(): Int {
        return newImages.size
    }

    inner class StartChildImageViewHolder(private val binding: ImageItemStartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(myImage: MyImage, position: Int) {

            Glide.with(binding.root)
                .load(myImage.matrix)
                .into(binding.imvChildImage)

            // Check isDownloaded to show or not
            checkCurrentImageInStorage(
                myImage.imageName,
                binding.cvIcDownloadStart,
            )

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
                    itemClickListener?.apply {
                        onDownloadImageToStorage(myImage, position)
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

    inner class EndChildImageViewHolder(private val binding: ImageItemEndBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(myImage: MyImage, position: Int) {

            Glide.with(binding.root)
                .load(myImage.matrix)
                .into(binding.imvChildImage)

            // Check isDownloaded to show or not
            checkCurrentImageInStorage(
                myImage.imageName,
                binding.cvIcDownload,
            )

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
                    itemClickListener?.apply {
                        onDownloadImageToStorage(myImage, position)
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

    inner class ChildImageViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bindFirstImage(image: Int, position: Int) {
            Glide.with(binding.root)
                .load(image)
                .into(binding.imvChildImage)
            binding.cvIcDownload.visibility = View.GONE

            binding.imvChildImage.setOnClickListener {
                // Logic for toggle item selected
                if (clickedPosition != position) {
                    notifyItemChanged(clickedPosition)
                    clickedPosition = position
                    notifyItemChanged(position)
                } else {
                    notifyItemChanged(position)
                }
                itemClickListener?.onClickButtonNone()
            }

            // Show Check Icon
            if (clickedPosition == position) {
                binding.imvDownloadedImage.visibility = View.VISIBLE
            } else {
                binding.imvDownloadedImage.visibility = View.GONE
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun bind(myImage: MyImage, position: Int) {

            // Show Images for api call
            Glide.with(binding.root)
                .load(myImage.matrix)
                .into(binding.imvChildImage)

            // Check isDownloaded to show or not
            checkCurrentImageInStorage(
                myImage.imageName,
                binding.cvIcDownload,
            )

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
                    itemClickListener?.apply {
                        onDownloadImageToStorage(myImage, position)
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

    fun getItemAtPosition(position: Int): MyImage {
        return newImages[position]
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            NORMAL_VIEW_TYPE
        } else {
            when (newImages[position].viewType) {
                START_VIEW_TYPE -> START_VIEW_TYPE
                NORMAL_VIEW_TYPE -> NORMAL_VIEW_TYPE
                END_VIEW_TYPE -> END_VIEW_TYPE
                else -> {
                    throw IllegalArgumentException("Invalid view type")
                }
            }
        }
    }

    private fun checkCurrentImageInStorage(
        name: String,
        cvIcDownload: CardView,
    ) {
        if(imageInfoList.isNotEmpty()) {
            for (imgInfo in imageInfoList) {
                if (name.equals(imgInfo.imageName, true)) {
                    cvIcDownload.visibility = View.GONE
                    Log.i(CommonConstant.MY_LOG_TAG, "$name and ${imgInfo.imageName}")
                }
            }
        }
    }

}
package com.phongnn.imagepicker.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.phongnn.imagepicker.R
import com.phongnn.imagepicker.databinding.TopicItemBinding

class TopicImageAdapter(
    private val topics: List<String>,
    private var itemClickListener: TopicClickListener?,
    private var scrollPosition: Int
) : RecyclerView.Adapter<TopicImageAdapter.TopicViewHolder>() {

    private var selectedPosition = RecyclerView.SCROLLBAR_POSITION_DEFAULT

    interface TopicClickListener {
        fun onItemClick(position: Int)
        fun onItemScroll(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = TopicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return topics.size
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.bind(topic, position)
    }

    inner class TopicViewHolder(binding: TopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val textView = binding.tvTopic
        @SuppressLint("NotifyDataSetChanged")
        fun bind(str: String, position: Int) {
            textView.text = str
            // Set text color based on position
            if (selectedPosition == position) {
                textView.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            textView.context,
                            R.color.selected_text_color
                        )
                    )
                    setTypeface(null, Typeface.BOLD)
                }
            } else {
                textView.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            textView.context,
                            R.color.default_text_color
                        )
                    )
                    setTypeface(null, Typeface.NORMAL)
                }
            }
            // Set On Click
            textView.setOnClickListener {
                selectedPosition = position
                itemClickListener?.onItemClick(position)
                if (scrollPosition != 0) {
                    selectedPosition = scrollPosition
                    itemClickListener?.onItemScroll(scrollPosition)
                }
                notifyDataSetChanged()
            }
        }

    }

}
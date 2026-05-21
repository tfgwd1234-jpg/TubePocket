package com.joo.tubepocket

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(private var videoList: List<VideoItem>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val tvTitle: TextView = itemView.findViewById(R.id.tvVideoTitle)
        val tvTags: TextView = itemView.findViewById(R.id.tvVideoTags)
        val tvMemo: TextView = itemView.findViewById(R.id.tvVideoMemo)
        val tvDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)
        val tvShortsBadge: TextView = itemView.findViewById(R.id.tvShortsBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = videoList[position]

        holder.tvTitle.text = item.title
        holder.tvTags.text = item.tags
        holder.tvMemo.text = item.memo
        holder.tvDuration.text = item.duration

        if (item.isShorts) {
            holder.tvShortsBadge.visibility = View.VISIBLE
        } else {
            holder.tvShortsBadge.visibility = View.GONE
        }

        if (item.thumbnailUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.thumbnailUrl)
                .into(holder.ivThumbnail)
        }

        // [추가된 클릭 이벤트] 아이템 클릭 시 상세 화면(VideoDetailActivity)으로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, VideoDetailActivity::class.java).apply {
                putExtra("title", item.title)
                putExtra("tags", item.tags)
                putExtra("memo", item.memo)
                putExtra("thumbnailUrl", item.thumbnailUrl)
                putExtra("timestamp", item.timestamp)
                putExtra("videoUrl", item.videoUrl)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    fun updateData(newList: List<VideoItem>) {
        this.videoList = newList
        notifyDataSetChanged()
    }
}
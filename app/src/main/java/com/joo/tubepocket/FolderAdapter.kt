package com.joo.tubepocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private var folderList: MutableList<FolderItem>,
    private var videoList: List<VideoItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolderName: TextView = view.findViewById(R.id.tvFolderName)
        val tvVideoCount: TextView = view.findViewById(R.id.tvVideoCount)
        val spaceIndent: View = view.findViewById(R.id.spaceIndent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]
        holder.tvFolderName.text = folder.name

        val count = videoList.count { it.tags.contains("#${folder.name}") }
        holder.tvVideoCount.text = count.toString()

        // [해결 포인트] 1단계는 30, 2단계는 60, 3단계는 90... 계단처럼 자동으로 들어갑니다!
        val params = holder.spaceIndent.layoutParams
        val density = holder.itemView.context.resources.displayMetrics.density
        params.width = (folder.depth * 30 * density).toInt()
        holder.spaceIndent.layoutParams = params

        holder.itemView.setOnClickListener {
            onItemClick(folder.name)
        }
    }

    override fun getItemCount() = folderList.size

    fun updateData(newFolders: MutableList<FolderItem>, newVideos: List<VideoItem>) {
        this.folderList = newFolders
        this.videoList = newVideos
        notifyDataSetChanged()
    }
}
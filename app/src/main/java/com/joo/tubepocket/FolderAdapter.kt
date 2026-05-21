package com.joo.tubepocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// [수정] 클릭 이벤트를 받을 수 있게 onItemClick 추가!
class FolderAdapter(
    private var folderList: MutableList<FolderItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolderName: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folderName = folderList[position].name
        holder.tvFolderName.text = folderName

        // [추가] 폴더를 클릭했을 때의 행동!
        holder.itemView.setOnClickListener {
            onItemClick(folderName)
        }
    }

    override fun getItemCount() = folderList.size

    // [추가] 파이어베이스에서 새 데이터가 오면 화면을 새로고침 하는 함수
    fun updateData(newList: MutableList<FolderItem>) {
        this.folderList = newList
        notifyDataSetChanged()
    }
}
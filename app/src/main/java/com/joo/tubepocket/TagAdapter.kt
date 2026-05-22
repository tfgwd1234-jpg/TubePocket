package com.joo.tubepocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 태그 리스트를 화면에 그려주는 어댑터입니다.
class TagAdapter(
    private var tagList: List<String>,
    private var videoList: List<VideoItem>,
    private val onItemClick: (String) -> Unit,        // 짧게 누르면 영상 검색
    private val onItemLongClick: (String) -> Unit     // 길게 누르면 수정/삭제
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입(화면 종류)을 구분하기 위한 번호입니다.
    private val TYPE_TAG = 0
    private val TYPE_FOOTER = 1

    // 1. 기존 태그를 보여주는 뷰홀더
    class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTagName: TextView = view.findViewById(R.id.tvTagName)
        val tvTagVideoCount: TextView = view.findViewById(R.id.tvTagVideoCount)
    }

    // 2. [새로 추가됨] 맨 밑 안내 문구를 보여주는 뷰홀더
    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 안내 문구는 누를 필요가 없어서 비워둡니다.
    }

    // 현재 그릴 칸이 '태그'인지 '안내 문구'인지 컴퓨터에게 알려줍니다.
    override fun getItemViewType(position: Int): Int {
        // 리스트의 맨 마지막 위치라면 푸터(안내 문구)를 보여주라고 명령합니다.
        return if (position == tagList.size) TYPE_FOOTER else TYPE_TAG
    }

    // 화면을 도화지에 그릴 때, 번호에 맞춰서 알맞은 디자인을 가져옵니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TAG) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag, parent, false)
            TagViewHolder(view)
        } else {
            // 👈 우리가 방금 새로 만든 안내 문구 디자인(item_tag_footer)을 불러옵니다.
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag_footer, parent, false)
            FooterViewHolder(view)
        }
    }

    // 각각의 칸에 실제 데이터와 클릭 기능을 연결합니다.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 태그 칸일 때만 글자를 채워주고 클릭 기능을 넣습니다.
        if (holder is TagViewHolder) {
            val tagName = tagList[position]
            holder.tvTagName.text = tagName

            // 해당 태그가 포함된 영상의 개수를 셉니다.
            val count = videoList.count { it.tags.contains(tagName) }
            holder.tvTagVideoCount.text = count.toString()

            // 짧게 눌렀을 때
            holder.itemView.setOnClickListener {
                onItemClick(tagName)
            }

            // 길게 눌렀을 때
            holder.itemView.setOnLongClickListener {
                onItemLongClick(tagName)
                true // true를 반환하면 짧은 클릭과 겹치지 않게 해줍니다.
            }
        }
        // FooterViewHolder(안내 문구)일 때는 아무것도 하지 않고 그냥 예쁘게 놔둡니다.
    }

    // 👈 [중요] 기존 태그 개수에 '안내 문구 1칸'을 추가로 더해줍니다.
    override fun getItemCount() = tagList.size + 1

    fun updateData(newTagList: List<String>, newVideoList: List<VideoItem>) {
        this.tagList = newTagList
        this.videoList = newVideoList
        notifyDataSetChanged()
    }
}
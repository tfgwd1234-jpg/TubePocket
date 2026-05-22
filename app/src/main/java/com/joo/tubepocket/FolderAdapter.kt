package com.joo.tubepocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private var allFolderList: MutableList<FolderItem>, // 전체 폴더 목록
    private var videoList: List<VideoItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    // 👈 [추가] 화면에 실제로 보여줄 폴더들만 담는 바구니
    private var visibleFolderList: MutableList<FolderItem> = mutableListOf()
    // 👈 [추가] 닫혀있는 폴더들의 이름을 기억하는 수첩
    private val collapsedFolderNames = mutableSetOf<String>()

    init {
        updateVisibleList()
    }

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolderName: TextView = view.findViewById(R.id.tvFolderName)
        val tvVideoCount: TextView = view.findViewById(R.id.tvVideoCount)
        val spaceIndent: View = view.findViewById(R.id.spaceIndent)
        // 👈 방금 XML에서 만든 화살표 버튼 연결! (안전하게 ?를 붙여줍니다)
        val tvToggle: TextView? = view.findViewById(R.id.tvToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        // [핵심] 전체가 아니라 눈에 보이는(visible) 리스트에서 꺼내옵니다!
        val folder = visibleFolderList[position]
        holder.tvFolderName.text = folder.name

        val count = videoList.count { it.tags.contains("#${folder.name}") }
        holder.tvVideoCount.text = count.toString()

        // 1단계는 30, 2단계는 60... 계단처럼 들여쓰기
        val params = holder.spaceIndent.layoutParams
        val density = holder.itemView.context.resources.displayMetrics.density
        params.width = (folder.depth * 30 * density).toInt()
        holder.spaceIndent.layoutParams = params

        // 👉 [요청 2번 반영] 화살표 열고 닫기 작동!
        val hasChildren = allFolderList.any { it.parentName == folder.name } // 내 밑에 자식이 있는지 검사

        if (holder.tvToggle != null) {
            if (hasChildren) {
                // 자식이 있으면 화살표를 보여줍니다.
                holder.tvToggle.visibility = View.VISIBLE
                if (collapsedFolderNames.contains(folder.name)) {
                    holder.tvToggle.text = "▶" // 수첩에 적혀있으면(닫혀있으면) 오른쪽 화살표
                } else {
                    holder.tvToggle.text = "▼" // 열려있으면 아래 화살표
                }
            } else {
                // 자식이 없으면 화살표를 투명하게 숨깁니다 (칸은 유지해서 글씨 줄이 삐뚤어지지 않게 해요!)
                holder.tvToggle.visibility = View.INVISIBLE
            }

            // 화살표를 클릭했을 때의 행동
            holder.tvToggle.setOnClickListener {
                if (collapsedFolderNames.contains(folder.name)) {
                    collapsedFolderNames.remove(folder.name) // 닫힌 거 열기
                } else {
                    collapsedFolderNames.add(folder.name) // 열린 거 닫기
                }
                updateVisibleList() // 화면 새로고침!
            }
        }

        // 폴더 이름을 클릭했을 때는 기존처럼 작동
        holder.itemView.setOnClickListener {
            onItemClick(folder.name)
        }
    }

    override fun getItemCount() = visibleFolderList.size // 눈에 보이는 폴더 개수만 알려줍니다!

    // [마법의 함수] 열려있는지 닫혀있는지 계산해서 화면에 보여줄 폴더만 골라내는 기능
    private fun updateVisibleList() {
        visibleFolderList.clear()
        var hiddenDepth = Int.MAX_VALUE // 얼마나 깊은 곳부터 숨길지 결정하는 변수

        for (folder in allFolderList) {
            // 숨겨야 하는 깊이보다 현재 폴더가 더 깊으면(자식 폴더라면) 건너뜁니다! (화면에 안 그림)
            if (folder.depth > hiddenDepth) {
                continue
            } else {
                // 자식이 끝나고 내 형제나 부모가 나오면 다시 숨김을 풉니다.
                hiddenDepth = Int.MAX_VALUE
            }

            visibleFolderList.add(folder)

            // 만약 이 폴더가 수첩에 적혀있다면(닫혀있다면), 자식들을 숨기도록 설정합니다.
            if (collapsedFolderNames.contains(folder.name)) {
                hiddenDepth = folder.depth
            }
        }
        notifyDataSetChanged() // 계산이 끝났으니 화면을 다시 그리라고 명령!
    }

    fun updateData(newFolders: MutableList<FolderItem>, newVideos: List<VideoItem>) {
        // [수정됨] 데이터가 새로 들어올 때마다 순서(orderIndex)대로 정렬을 한 번 더 해줘서 절대 순서가 꼬이지 않게 강력하게 보호합니다!
        this.allFolderList = newFolders.sortedBy { it.orderIndex }.toMutableList()
        this.videoList = newVideos
        updateVisibleList() // 데이터가 바뀌면 화면도 다시 계산
    }
}
package com.joo.tubepocket

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.OnBackPressedCallback

class FolderFragment : Fragment(R.layout.fragment_folder_view) {

    private lateinit var rvFolderList: RecyclerView
    private lateinit var adapter: FolderAdapter
    private var currentFolderList = mutableListOf<FolderItem>()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        rvFolderList = view.findViewById(R.id.rvFolderList)
        rvFolderList.layoutManager = LinearLayoutManager(context)

        adapter = FolderAdapter(currentFolderList, emptyList()) { clickedFolderName ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorageFragment.newInstance(clickedFolderName))
                .addToBackStack(null)
                .commit()
        }
        rvFolderList.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(rvFolderList)

        sharedViewModel.videoList.observe(viewLifecycleOwner) { videos ->
            adapter.updateData(currentFolderList, videos)
        }

        // [핵심 해결 2] 부모-자식을 3단계, 4단계 무한으로 찾아주는 족보 탐색기!
        sharedViewModel.folderList.observe(viewLifecycleOwner) { folders ->
            // 1. 번호표(orderIndex) 순서대로 줄을 세워요
            val sortedByOrder = folders.sortedBy { it.orderIndex }
            val resultList = mutableListOf<FolderItem>()

            // 2. 부모 밑에 자식, 자식 밑에 손자를 무한으로 찾아주는 마법 함수
            fun addChildren(parentName: String, currentDepth: Int) {
                val children = sortedByOrder.filter { it.parentName == parentName }
                for (child in children) {
                    child.depth = currentDepth // 몇 단계인지 도장 꾹!
                    resultList.add(child)
                    addChildren(child.name, currentDepth + 1) // 내 자식도 찾아와라! (무한 반복)
                }
            }

            addChildren("", 0) // 가장 바깥(최상위) 폴더부터 찾기 시작!
            currentFolderList = resultList.toMutableList()
            adapter.updateData(currentFolderList, sharedViewModel.videoList.value ?: emptyList())
        }

        val btnAddFolder = view.findViewById<TextView>(R.id.btnAddFolder)
        btnAddFolder.setOnClickListener {
            FolderHelper.showAddFolderDialog(requireContext()) { newFolderName ->
                // 새 폴더는 맨 밑으로 가도록 리스트 크기만큼의 번호표를 줍니다.
                sharedViewModel.addFolder(FolderItem(newFolderName, "", currentFolderList.size, 0))
                Toast.makeText(requireContext(), "'$newFolderName' 폴더가 생성되었습니다!", Toast.LENGTH_SHORT).show()
            }
        }
        // 3. 폴더보기 화면에서 휴대폰 물리 뒤로가기를 누르면 기본 보관함("모든 영상")으로 이동합니다.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, StorageFragment())
                    .commit()
            }
        })
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            val item = currentFolderList.removeAt(fromPosition)
            currentFolderList.add(toPosition, item)
            adapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        // [핵심 해결 1] 폴더를 드래그하고 손가락을 딱! 뗄 때 실행됩니다.
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val droppedFolder = currentFolderList[position]

            // 목표 1: 부모 폴더를 옮겼다면, 하위 폴더들을 꼬리처럼 전부 데려오기!
            fun collectAllChildren(parentName: String): List<FolderItem> {
                val children = currentFolderList.filter { it.parentName == parentName }
                val result = mutableListOf<FolderItem>()
                for (child in children) {
                    result.add(child)
                    result.addAll(collectAllChildren(child.name))
                }
                return result
            }

            val allChildren = collectAllChildren(droppedFolder.name)
            if (allChildren.isNotEmpty()) {
                currentFolderList.removeAll(allChildren)
                val newParentIndex = currentFolderList.indexOf(droppedFolder)
                currentFolderList.addAll(newParentIndex + 1, allChildren) // 부모 바로 밑줄에 착착 세우기
            }

            // 목표 2: 자식 폴더를 엉뚱한(부모 밖) 곳으로 꺼냈다면 독립시키기!
            if (droppedFolder.parentName.isNotEmpty()) {
                val itemAbove = if (position > 0) currentFolderList[position - 1] else null

                var isStillUnderParent = false
                var checkFolder = itemAbove

                // 내 윗줄 폴더의 족보를 따라 올라가면서 진짜 가족이 맞는지 검사해요
                while (checkFolder != null) {
                    if (checkFolder.name == droppedFolder.parentName) {
                        isStillUnderParent = true
                        break
                    }
                    checkFolder = currentFolderList.find { it.name == checkFolder?.parentName }
                }

                // 가족을 벗어났다면 부모 이름 지우기 (독립!)
                if (!isStillUnderParent) {
                    droppedFolder.parentName = ""
                    Toast.makeText(context, "'${droppedFolder.name}' 폴더가 밖으로 나왔어요!", Toast.LENGTH_SHORT).show()
                }
            }

            // 목표 3: 바뀐 순서(번호표)를 파이어베이스 서버에 한꺼번에 싹 저장하기!
            currentFolderList.forEachIndexed { index, folder ->
                if (folder.orderIndex != index) {
                    folder.orderIndex = index
                    sharedViewModel.addFolder(folder)
                }
            }

            adapter.notifyDataSetChanged()
        }

        // 스와이프 기능 (기존 그대로 유지)
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = currentFolderList[position]

            if (direction == ItemTouchHelper.RIGHT) {
                if (position > 0) {
                    val parentFolder = currentFolderList[position - 1]
                    item.parentName = parentFolder.name
                    sharedViewModel.addFolder(item)
                    Toast.makeText(context, "${parentFolder.name} 안으로 들어갔어요!", Toast.LENGTH_SHORT).show()
                }
            } else if (direction == ItemTouchHelper.LEFT) {
                item.parentName = ""
                sharedViewModel.addFolder(item)
                Toast.makeText(context, "밖으로 꺼냈어요!", Toast.LENGTH_SHORT).show()
            }
            adapter.notifyItemChanged(position)
        }
    }
}
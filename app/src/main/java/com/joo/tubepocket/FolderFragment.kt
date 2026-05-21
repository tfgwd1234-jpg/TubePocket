package com.joo.tubepocket

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // [중요] 이 줄이 없어서 에러가 났어요!
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class FolderFragment : Fragment(R.layout.fragment_folder_view) {

    private lateinit var rvFolderList: RecyclerView
    private lateinit var adapter: FolderAdapter

    // 폴더 리스트를 담아둘 안전한 상자 (중복 에러 해결)
    private var currentFolderList = mutableListOf<FolderItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뷰모델 가져오기
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 2. 리사이클러뷰 준비
        rvFolderList = view.findViewById(R.id.rvFolderList)
        rvFolderList.layoutManager = LinearLayoutManager(context)

        // 3. 어댑터 연결 (중복된 코드 삭제하고 올바른 코드로 하나만 남김)
        adapter = FolderAdapter(currentFolderList) { clickedFolderName ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorageFragment.newInstance(clickedFolderName))
                .addToBackStack(null) // 폰의 뒤로가기를 누르면 다시 폴더 목록으로 돌아옵니다
                .commit()
        }
        rvFolderList.adapter = adapter

        // 4. 드래그 앤 드롭 기능 연결
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(rvFolderList)

        // 5. 파이어베이스 데이터 관찰해서 화면에 그리기
        sharedViewModel.folderList.observe(viewLifecycleOwner) { folders ->
            currentFolderList = ArrayList(folders) // 최신 데이터로 업데이트
            adapter.updateData(currentFolderList)
        }

        // 6. 폴더 추가 버튼 (중복 선언된 btnAddFolder 해결)
        val btnAddFolder = view.findViewById<TextView>(R.id.btnAddFolder)
        btnAddFolder.setOnClickListener {
            FolderHelper.showAddFolderDialog(requireContext()) { newFolderName ->
                sharedViewModel.addFolder(FolderItem(newFolderName))
                Toast.makeText(requireContext(), "'$newFolderName' 폴더가 저장되었습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 드래그 앤 드롭 동작 (기존과 동일하게 안전하게 유지)
    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            // 리스트 순서 바꾸기 및 어댑터 알림
            val item = currentFolderList.removeAt(fromPosition)
            currentFolderList.add(toPosition, item)
            adapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
}
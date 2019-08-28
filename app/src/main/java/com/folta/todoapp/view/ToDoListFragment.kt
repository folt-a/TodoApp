package com.folta.todoapp.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.data.local.ToDoRepository
import com.folta.todoapp.data.local.ToDoRepositoryLocal
import kotlinx.android.synthetic.main.fragment_todo_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ToDoListFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var todoAdapter: ToDoAdapter
    private lateinit var repository: ToDoRepository

    private lateinit var viewToDoList: MutableList<ToDo>

    private val job = Job()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this.context)

        CoroutineScope(Dispatchers.Main + job).launch {
            repository = ToDoRepositoryLocal(view.context)
            viewToDoList = repository.findByDate("").toMutableList()
            todoAdapter = ToDoAdapter(viewToDoList)
            recycleView.adapter = todoAdapter
            todoAdapter.notifyDataSetChanged()
        }

        // 区切り線の表示
        recycleView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )

        fab.setOnClickListener {
            val inputMethodManager =
            ContextCompat.getSystemService(it!!.context, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            val orderId: Int
            if (todoAdapter.itemCount != 0) {
                orderId = viewToDoList.maxBy { toDo -> toDo.orderId }!!.orderId + 1
            } else {
                orderId = 1
            }
            val todo =
                ToDo(
                    id = 0,
                    isChecked = false,
                    content = "ああああああああああああ\nbbbbbbb\nえええええccccccccccccccccc",
                    title = "titleView",
                    createdAt = "",
                    orderId = orderId
                )
            CoroutineScope(Dispatchers.Main + job).launch {
                val savedId = repository.save(todo)
                val savedToDo = repository.find(savedId)
                viewToDoList.add(savedToDo)
                todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
            }
        }

        toolbar.setOnTouchListener { v, _ ->
            val inputMethodManager =
                ContextCompat.getSystemService(v!!.context, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            toolbar.requestFocus()
        }

        val getRecyclerViewSimpleCallBack =
            // 引数で、上下のドラッグ、および左方向のスワイプを有効にしている。
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder?.adapterPosition ?: 0
                    val toPosition = target?.adapterPosition ?: 0

                    viewToDoList.add(toPosition, viewToDoList.removeAt(fromPosition))
                    todoAdapter.notifyItemMoved(fromPosition, toPosition)
                    Logger.d("  ")
                    for (i in viewToDoList) {
                        Logger.d("id:" + i.id + " oId:" + i.orderId)
                    }
                    return true
                }

                // スワイプしたとき
                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                    p0.let {
                        CoroutineScope(Dispatchers.Main + job).launch {
                            // 実データセットからアイテムを削除
                            repository.delete(todoAdapter.items[p0.adapterPosition].id)
                            Logger.d("delete : ${todoAdapter.items[p0.adapterPosition].id}")
                            viewToDoList.removeAt(p0.adapterPosition)
                            Logger.d("remove : ${p0.adapterPosition}")
                            todoAdapter.notifyItemRemoved(p0.adapterPosition)
                        }
                    }
                }
            }

        val itemTouchHelper = ItemTouchHelper(getRecyclerViewSimpleCallBack)
        itemTouchHelper.attachToRecyclerView(recycleView)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onStop() {
        Logger.d("onStop")
        CoroutineScope(Dispatchers.Main + job).launch {
            repository.saveSort(viewToDoList)
            Logger.d("List Saved!!")

            viewToDoList = repository.findByDate("").toMutableList()
            todoAdapter.items = viewToDoList
            todoAdapter.notifyDataSetChanged()
        }
        super.onStop()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}

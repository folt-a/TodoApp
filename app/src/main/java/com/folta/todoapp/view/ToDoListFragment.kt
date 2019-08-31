package com.folta.todoapp.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
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
import kotlinx.android.synthetic.main.holder_todo.view.*
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

            todoAdapter = object : ToDoAdapter(viewToDoList) {
                override fun onClick(v: View?, holder: ToDoViewHolder) {
                    if (holder.isShowDetail) {
                        holder.title.requestFocus()
                    } else {
                        holder.linearLayout.requestFocus()
                        holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    }
                }

                override fun onDoneCheck(v: CompoundButton?, holder: ToDoViewHolder) {
                    val todo = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (todo != null) repository.save(todo)
                    }
                }

                override fun onTitleClick(v: View?, holder: ToDoViewHolder) {
                    holder.title.isFocusable = true
                    holder.title.isFocusableInTouchMode = true
                    holder.title.requestFocus()
                    holder.inputMethodManager?.showSoftInput(v, 1)
                }

                override fun onTitleFocusChange(
                    v: View?,
                    hasFocus: Boolean,
                    holder: ToDoViewHolder
                ): Boolean {
                    if (hasFocus) return true
                    val todo = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (todo != null) repository.save(todo)
                    }
                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    return false
                }

                override fun onTitleEditorAction(
                    v: TextView?,
                    actionId: Int,
                    holder: ToDoViewHolder
                ): Boolean {
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            holder.title.isFocusable = false
                            holder.title.isFocusableInTouchMode = false
                            holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                            return true
                        }
                        else -> {
                            return false
                        }
                    }
                }

                override fun showContentDetail(v: View?, holder: ToDoViewHolder) {
                    //        contentを全文表示する
                    holder.content.setText(items[holder.adapterPosition].content)
                    holder.content.setBackgroundResource(R.drawable.edittext_content)
                    v?.let {
                        holder.content.setPadding(
                            v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                            v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                            v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                            v.context.resources.getDimensionPixelSize(R.dimen.dp8)
                        )
                    }
                    holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    val mlp = holder.itemView.content.layoutParams
                    if (mlp is ViewGroup.MarginLayoutParams) {
                        v?.let {
                            mlp.setMargins(
                                0,
                                v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                                0,
                                0
                            )
                        }
                    }
                    holder.detail.setImageResource(R.drawable.ic_detail_selected)
                    holder.content.visibility = View.VISIBLE
                    holder.content.isEnabled = !holder.content.isEnabled
                    holder.content.isFocusable = !holder.content.isFocusable
                    holder.content.isFocusableInTouchMode = !holder.content.isFocusableInTouchMode

                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    holder.isShowDetail = !holder.isShowDetail
                }

                @SuppressLint("SetTextI18n")
                override fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
                    Logger.d("closeContentDetail")
//      本文は改行削除＋入らない部分は非表示にする
                    val pos = holder.adapterPosition
                    if (items[pos].content.length > 20) {
                        holder.content.setText(
                            "${items[pos].content.replace(
                                "\n",
                                " "
                            ).substring(0..20)}..."
                        )
                        holder.content.visibility = View.VISIBLE
                    } else if (items[pos].content.isEmpty()) {
                        holder.content.visibility = View.GONE
                    } else {
                        holder.content.visibility = View.VISIBLE
                        holder.content.setText(items[pos].content)
                    }

                    holder.content.background = null
                    holder.content.setPadding(
                        0,
                        0,
                        0,
                        0
                    )
                    holder.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    val mlp = holder.content.layoutParams
                    if (mlp is ViewGroup.MarginLayoutParams) mlp.setMargins(0)
                    holder.detail.setImageResource(R.drawable.ic_detail)

                    holder.content.isEnabled = !holder.content.isEnabled
                    holder.content.isFocusable = !holder.content.isFocusable
                    holder.content.isFocusableInTouchMode = !holder.content.isFocusableInTouchMode

                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    holder.isShowDetail = !holder.isShowDetail
                }

                override fun onContentFocusChange(
                    v: View?,
                    hasFocus: Boolean,
                    holder: ToDoViewHolder
                ): Boolean {
                    Logger.d("onContentFocusChange")
                    if (hasFocus) return true
                    val todo = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (todo != null) repository.save(todo)
                    }
                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    return false
                }
            }
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
                    content = "",
                    title = "",
                    createdAt = "",
                    orderId = orderId
                )
            CoroutineScope(Dispatchers.Main + job).launch {
                val savedId = repository.save(todo)
                val savedToDo = repository.find(savedId)
                viewToDoList.add(savedToDo)
                todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
                todoAdapter.notifyDataSetChanged()
//                val addedHolder= recycleView.findViewHolderForAdapterPosition(todoAdapter.itemCount - 1)
                val addedHolder = recycleView.findViewHolderForAdapterPosition(1)
                Logger.d("addedHolder" + (addedHolder as? ToDoAdapter.ToDoViewHolder)?.title)
//                Logger.d("addedHolder" + recycleView.findViewHolderForAdapterPosition(todoAdapter.itemCount - 1).toString())
//                (addedHolder as? ToDoAdapter.ToDoViewHolder)?.title?.requestFocus()

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
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition

                    viewToDoList.add(toPosition, viewToDoList.removeAt(fromPosition))
                    todoAdapter.notifyItemMoved(fromPosition, toPosition)
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

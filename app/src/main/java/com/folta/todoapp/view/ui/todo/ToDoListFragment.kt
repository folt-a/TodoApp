package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.TagRepository
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.data.local.ToDoRepository
import com.folta.todoapp.view.TodoActivity
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.synthetic.main.fragment_todo_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.koin.android.ext.android.inject

class ToDoListFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var todoAdapter: ToDoAdapter
    private val todoRepository by inject<ToDoRepository>()

    private val tagRepository by inject<TagRepository>()

    private lateinit var viewToDoList: MutableList<ToDo>
    private lateinit var titleDate: LocalDate

    private val job = Job()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_todo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.calendar -> {
                Toast.makeText(context, "Calender Click!", Toast.LENGTH_SHORT).show()
                val picker = this.context?.let { it -> DatePickerDialog(it) }
                picker?.setOnDateSetListener { _, year, month, dayOfMonth ->
                    titleDate = LocalDate.of(year, month + 1, dayOfMonth)
                    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                    (activity as TodoActivity).setActionBarTitle(titleDate.format(formatter))
                    CoroutineScope(Dispatchers.Main + job).launch {
                        viewToDoList =
                            todoRepository.findByDate(titleDate.toStringyyyyMMdd())
                                .toMutableList()
                        todoAdapter.items = viewToDoList
                        todoAdapter.notifyDataSetChanged()
                    }
                }
                picker?.updateDate(titleDate.year, titleDate.monthValue - 1, titleDate.dayOfMonth)
                picker?.show()
            }
        }
        return true
    }

    private fun LocalDate.toStringyyyyMMdd(): String =
        "${this.year}${this.monthValue + 1}${this.dayOfMonth}"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        titleDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        (activity as TodoActivity).setActionBarTitle(titleDate.format(formatter))

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this.context)
        recycleView.setOnTouchListener { v, event ->
            //            Logger.d(event.action.toString())
            if (event.action == MotionEvent.ACTION_DOWN) {
                ContextCompat.getSystemService(v.context, InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(v?.windowToken, 0)
                coordinatorLayout.requestFocus()
            }
            return@setOnTouchListener false
        }

        CoroutineScope(Dispatchers.Main + job).launch {
            viewToDoList =
                todoRepository.findByDate(titleDate.toStringyyyyMMdd())
                    .toMutableList()
            val tagList = tagRepository.getAll().toMutableList()
            todoAdapter = object : ToDoAdapter(viewToDoList, tagList) {
                override fun onClick(v: View?, holder: ToDoViewHolder) {
                    if (holder.isShowDetail) {
                        holder.title.requestFocus()
                    } else {
                        holder.linearLayout.requestFocus()
                        fab.visibility = View.VISIBLE
                        holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    }
                }

                override fun onDoneCheck(v: CompoundButton?, holder: ToDoViewHolder) {
                    val todo = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (todo != null) todoRepository.save(todo)
                    }
                }

                override fun onTitleClick(v: View?, holder: ToDoViewHolder) {
                    holder.title.isFocusable = true
                    holder.title.isFocusableInTouchMode = true
                    holder.title.requestFocus()
                    fab.visibility = View.GONE
                    holder.inputMethodManager?.showSoftInput(v, 1)
                }

                override fun onTitleFocusChange(
                    v: View?,
                    hasFocus: Boolean,
                    holder: ToDoViewHolder
                ): Boolean {
                    if (hasFocus) {
                        return holder.title.performClick()
                    } else {
                        val todo = getEditedToDo(holder)
                        CoroutineScope(Dispatchers.Main + job).launch {
                            if (todo != null) todoRepository.save(todo)
                        }
                        fab.visibility = View.VISIBLE
                        holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                        return true
                    }
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
                            fab.visibility = View.VISIBLE
                            holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                            return true
                        }
                        else -> {
                            return false
                        }
                    }
                }

                override fun onSpinnerSelected(v: View?, id: Int, holder: ToDoViewHolder) {
                    Logger.d("スピナー onItemSelected id = $id")
                    val todo = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (todo != null) {
                            todoRepository.save(todo)

//                        タグ変更されたので描画やりなおし
                            var tag = tagList.firstOrNull { it.id == todo.tagId }
//                        タグなし、削除済みタグは未設定タグとして描画する
                            if (tag == null || tag.isDeleted) {
                                tag = tagList[0]
                            }
                            val colorResId = tag.color
                            val patternResId = tag.pattern
                            if (v != null) {
                                val drawable = TileDrawable.create(
                                    v.context,
                                    colorResId,
                                    patternResId,
                                    Shader.TileMode.REPEAT
                                )
                                holder.todoTag.setImageDrawable(drawable)
                            }

                        }
                    }
                }

                override fun showContentDetail(v: View?, holder: ToDoViewHolder) {
                    holder.tagTextView.visibility = View.VISIBLE
                    holder.tagSpinner.visibility = View.VISIBLE
                    holder.content.fullText = items[holder.adapterPosition].content
                    holder.content.openMemo()
                    holder.content.visibility = View.VISIBLE
                    holder.detail.setImageResource(R.drawable.ic_detail_selected)
                    fab.visibility = View.VISIBLE
                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    holder.isShowDetail = !holder.isShowDetail
                }

                override fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
                    onContentFocusChange(v, false, holder)

                    holder.tagTextView.visibility = View.GONE
                    holder.tagSpinner.visibility = View.GONE
                    holder.content.fullText = items[holder.adapterPosition].content
                    holder.content.closeMemo()
                    holder.detail.setImageResource(R.drawable.ic_detail)
                    fab.visibility = View.VISIBLE
                    holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                    holder.isShowDetail = !holder.isShowDetail
                }

                override fun onContentClick(v: View?, holder: ToDoViewHolder) {
                    Logger.d("onContentClick")
                    fab.visibility = View.GONE
                    holder.inputMethodManager?.showSoftInput(v, 1)
                }

                override fun onContentFocusChange(
                    v: View?,
                    hasFocus: Boolean,
                    holder: ToDoViewHolder
                ): Boolean {
                    if (hasFocus) {
                        return holder.content.performClick()
                    } else {
                        holder.content.fullText = holder.content.text.toString()
                        Logger.d("onContentFocusChange : $hasFocus")
                        val todo = getEditedToDo(holder)
                        CoroutineScope(Dispatchers.Main + job).launch {
                            if (todo != null) todoRepository.save(todo)
                        }
                        fab.visibility = View.VISIBLE
                        holder.inputMethodManager?.hideSoftInputFromWindow(v?.windowToken, 0)
                        return true
                    }
                }
            }
            todoAdapter.setHasStableIds(true)
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
                    tagId = 0,
                    createdAt = "${titleDate.year}${titleDate.monthValue + 1}${titleDate.dayOfMonth}",
                    orderId = orderId
                )
            CoroutineScope(Dispatchers.Main + job).launch {
                val savedId = todoRepository.save(todo)
                val savedToDo = todoRepository.find(savedId)
                savedToDo?.let { it ->
                    viewToDoList.add(it)
                    todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
                }
            }
        }

        coordinatorLayout.setOnTouchListener { v, _ ->
            val inputMethodManager =
                ContextCompat.getSystemService(v!!.context, InputMethodManager::class.java)
            fab.visibility = View.VISIBLE
            inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            coordinatorLayout.requestFocus()
        }

        val getRecyclerViewSimpleCallBack =
            // 引数で、上下のドラッグ、および左方向のスワイプを有効にしている。
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL
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

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return makeMovementFlags(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                        0
                    )
                }

                // スワイプしたとき
                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
//                    p0.let {
//                        CoroutineScope(Dispatchers.Main + job).launch {
//                            // 実データセットからアイテムを削除
//                            todoRepository.delete(todoAdapter.items[p0.adapterPosition].id)
//                            Logger.d("delete : ${todoAdapter.items[p0.adapterPosition].id}")
//                            viewToDoList.removeAt(p0.adapterPosition)
//                        }
//                    }
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
            todoRepository.saveSort(viewToDoList)
            Logger.d("List Saved!!")
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

//class ToDoDatePickerDialog(
//    context: Context,
//    dateSetListener: OnDateSetListener
//) :
//    DatePickerDialog(context) {
//    //    var year: Int
////    var month: Int
////    var dayOfMonth: Int
//    init {
//        val calendar = Calendar.getInstance()
//        this.datePicker.maxDate = calendar.timeInMillis
//        this.setOnDateSetListener { view, year, month, dayOfMonth ->
//        title
//        }
//    }
//}
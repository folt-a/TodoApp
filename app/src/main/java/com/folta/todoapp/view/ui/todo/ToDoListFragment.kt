package com.folta.todoapp.view.ui.todo

import android.content.Context
import android.graphics.Shader
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
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
import com.folta.todoapp.view.MainActivity
import com.folta.todoapp.view.ui.TileDrawable
import com.folta.todoapp.view.ui.setOnSafeClickListener
import com.wdullaer.materialdatetimepicker.Utils
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_todo_list.*
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext

class ToDoListFragment : Fragment(), TodoContract.View, CoroutineScope,
    DatePickerDialog.OnDateSetListener {

    override var presenter: TodoContract.Presenter = TodoPresenter(this)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var todoAdapter: ToDoAdapter
    private val todoRepository by inject<ToDoRepository>()

    private val tagRepository by inject<TagRepository>()

    private lateinit var menu: Menu

    private lateinit var viewToDoList: MutableList<ToDo>
    private lateinit var titleDate: LocalDate
    private val job = Job()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.menu_todo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.calendar -> {
                onClickCalendarOptionMenu()
            }
            R.id.fixToDo -> {
                closeKeyboard(view)
            }
            R.id.deleteButton -> {
                onClickDeleteOptionMenu(item)
            }
        }
        return true
    }

    private fun onClickDeleteOptionMenu(menuItem: MenuItem) {
        when (todoAdapter.state) {
            ToDoAdapter.ListShowState.NORMAL -> {
                todoAdapter.state = ToDoAdapter.ListShowState.DELETE
                menuItem.setIcon(R.drawable.ic_check)
            }
            else -> {
                todoAdapter.state = ToDoAdapter.ListShowState.NORMAL
                menuItem.setIcon(R.drawable.ic_trash)
            }
        }
        todoAdapter.notifyDataSetChanged()
    }

    private fun onClickCalendarOptionMenu() {
        launch(Dispatchers.IO) {
            val picker = DatePickerDialog.newInstance(
                this@ToDoListFragment,
                titleDate.year,
                titleDate.monthValue - 1,
                titleDate.dayOfMonth
            )
            val strDates = todoRepository.getExistsDate()
            Logger.d("onClickCalendarOptionMenu highlightedDays $strDates")
            withContext(Dispatchers.Main) {
                if (strDates.isNotEmpty()) {
                    val array = arrayOfNulls<Calendar>(strDates.size)
                    strDates.forEachIndexed { index, s ->
                        val date = LocalDate.parse(s, formatter)
                        val calendar = Calendar.getInstance(TimeZone.getDefault())
                        calendar.set(Calendar.YEAR, date.year)
                        calendar.set(Calendar.MONTH, date.monthValue - 1)
                        calendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                        Utils.trimToMidnight(calendar)
                        array[index] = calendar
                    }
                    picker.highlightedDays = array
                }
                context?.let { picker.setCancelColor(ContextCompat.getColor(it, R.color.darkGray)) }
                picker.dismissOnPause(true)
                picker.setTitle("Select Date!!☆彡")
                activity?.supportFragmentManager?.let { picker.show(it, "Datepickerdialog") }
            }
        }
    }


    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        titleDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
        (activity as MainActivity).setActionBarTitle(titleDate.format(formatter))
        launch(Dispatchers.IO) {
            Logger.d("in IO onClickCalendarOptionMenu")

            viewToDoList =
                todoRepository.findByDate(titleDate.toStringyyyyMMdd()).toMutableList()
            withContext(Dispatchers.Main) {
                Logger.d("in withContext onClickCalendarOptionMenu")
                todoAdapter.items = viewToDoList
                todoAdapter.notifyDataSetChanged()
            }
        }
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    private fun LocalDate.toStringyyyyMMdd(): String {
        return formatter.format(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        titleDate = LocalDate.now()
        (activity as MainActivity).setActionBarTitle(titleDate.format(formatter))

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(view.context)
//        recycleView.setOnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                closeKeyboard(v)
//            }
//            return@setOnTouchListener false
//        }

        launch(Dispatchers.IO) {
            Logger.d("in IO onViewCreated")

            viewToDoList = withContext(Dispatchers.Default) {
                Logger.d("in withContext 111 onViewCreated")
                todoRepository.findByDate(titleDate.toStringyyyyMMdd()).toMutableList()
            }

            val tagList = withContext(Dispatchers.Default) {
                Logger.d("in withContext onViewCreated")
                tagRepository.getAll().toMutableList()
            }

            withContext(Dispatchers.Main) {
                todoAdapter = object : ToDoAdapter(viewToDoList, tagList) {
                    override fun onClick(v: View?, holder: ToDoViewHolder) {
                        if (holder.isShowDetail) {
                            title.requestFocus()
                        } else {
                            closeKeyboard(v)
                        }
                    }

                    override fun onDoneCheck(v: CompoundButton?, holder: ToDoViewHolder) {
                        val todo = getEditedToDo(holder)
                        launch(job + Dispatchers.IO) {
                            Logger.d("in IO onDoneCheck")

                            if (todo != null) todoRepository.save(todo)
                        }
                    }

                    override fun onTitleClick(v: View?, holder: ToDoViewHolder) {
                        openKeyboard(v)
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
                            launch(job + Dispatchers.IO) {
                                Logger.d("in IO onTitleFocusChange")
                                if (todo != null) todoRepository.save(todo)
                            }
                            closeKeyboard(v)
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
                                closeKeyboard(v)
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
                        if (todo != null) {
                            launch(job + Dispatchers.IO) {
                                Logger.d("in IO onSpinnerSelected")

                                todoRepository.save(todo)
                                withContext(Dispatchers.Main) {
                                    Logger.d("in withContext onSpinnerSelected")
                                    // タグ変更されたので描画やりなおし
                                    var tag = tagList.firstOrNull { it.id == todo.tagId }
                                    // タグなし、削除済みタグは未設定タグとして描画する
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
                    }

                    override fun onClickDetail(v: View?, holder: ToDoViewHolder) {
                        if (todoAdapter.state == ListShowState.DELETE) {
                            onClickDelete(v, holder)
                            return
                        }
                        if (holder.isShowDetail) {
                            closeContentDetail(v, holder)
                        } else {
                            showContentDetail(v, holder)
                        }
                    }

                    override fun onClickDelete(v: View?, holder: ToDoViewHolder) {
                        launch(job + Dispatchers.IO) {
                            // 実データセットからアイテムを削除
                            todoRepository.delete(todoAdapter.items[holder.adapterPosition].id)
                            Logger.d("delete : ${todoAdapter.items[holder.adapterPosition].id}")
                            viewToDoList.removeAt(holder.adapterPosition)
                            withContext(Dispatchers.Main) {
                                todoAdapter.notifyItemRemoved(holder.adapterPosition)
                            }
                        }
                    }

                    override fun showContentDetail(v: View?, holder: ToDoViewHolder) {
                        holder.tagTextView.visibility = View.VISIBLE
                        holder.tagSpinner.visibility = View.VISIBLE
                        holder.content.fullText = items[holder.adapterPosition].content
                        holder.content.openMemo()
                        val icon = holder.detail.icon as AnimatedVectorDrawable
                        icon.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                holder.detail.setIconResource(R.drawable.ic_detail_selected)
                            }
                        })
                        if (!icon.isRunning) icon.start()
                        holder.isShowDetail = true
                        closeKeyboard(v)
                    }

                    override fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
                        onContentFocusChange(v, false, holder)

                        holder.tagTextView.visibility = View.GONE
                        holder.tagSpinner.visibility = View.GONE
                        holder.content.fullText = items[holder.adapterPosition].content
                        holder.content.closeMemo()
                        val icon = holder.detail.icon as AnimatedVectorDrawable
                        icon.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                holder.detail.setIconResource(R.drawable.ic_detail)
                            }
                        })
                        if (!icon.isRunning) icon.start()
                        holder.isShowDetail = false
                        closeKeyboard(v)
                    }

                    override fun onContentClick(v: View?, holder: ToDoViewHolder) {
                        Logger.d("onContentClick")
                        openKeyboard(v)
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
                            launch(job + Dispatchers.IO) {
                                Logger.d("in IO onContentFocusChange ")

                                if (todo != null) todoRepository.save(todo)
                            }
                            closeKeyboard(v)
                            return true
                        }
                    }
                }
                todoAdapter.setHasStableIds(true)
                recycleView.adapter = todoAdapter
                todoAdapter.notifyDataSetChanged()
            }
        }

        // 区切り線の表示
        recycleView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )

        fab.setOnSafeClickListener {
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
                    createdAt = titleDate.toStringyyyyMMdd(),
                    orderId = orderId
                )
            launch(Dispatchers.IO) {
                Logger.d("in IO fab.setOnSafeClickListener ")

                val savedId = todoRepository.save(todo)
                val savedTodo = todoRepository.find(savedId)
                withContext(Dispatchers.Main) {
                    Logger.d("in withContext fab.setOnSafeClickListener ")
                    if (savedTodo != null) viewToDoList.add(savedTodo)
                    todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
                }
            }
        }

        coordinatorLayout.setOnTouchListener { v, _ ->
            closeKeyboard(v)
            return@setOnTouchListener true
        }

        val getRecyclerViewSimpleCallBack =
            // 引数で、上下のドラッグを有効にしている。
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
                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {}
            }
        val itemTouchHelper = ItemTouchHelper(getRecyclerViewSimpleCallBack)
        itemTouchHelper.attachToRecyclerView(recycleView)
    }

    private fun openKeyboard(view: View?) {
        fab.hide()
        menu.findItem(R.id.fixToDo).isVisible = true
        menu.findItem(R.id.calendar).isVisible = false
        menu.findItem(R.id.deleteButton).isVisible = false
        context?.let { ContextCompat.getSystemService(it, InputMethodManager::class.java) }
            ?.showSoftInput(view, 1)
    }

    private fun closeKeyboard(view: View?) {
        coordinatorLayout.requestFocus()
        fab.show()
        menu.findItem(R.id.fixToDo).isVisible = false
        menu.findItem(R.id.calendar).isVisible = true
        menu.findItem(R.id.deleteButton).isVisible = true
        context?.let { ContextCompat.getSystemService(it, InputMethodManager::class.java) }
            ?.hideSoftInputFromWindow(view?.windowToken, 0)
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
        launch(Dispatchers.IO) {
            Logger.d("in IO onStop")

            todoRepository.saveSort(viewToDoList)
            Logger.d("List Saved!!")
        }
        super.onStop()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}

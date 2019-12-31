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
import com.folta.todoapp.Utility.Companion.toStringSlash_yyyyMMdd
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.*
import com.folta.todoapp.view.MainActivity
import com.folta.todoapp.view.ui.TileDrawable
import com.folta.todoapp.view.ui.setOnSafeClickListener
import com.folta.todoapp.view.ui.todo.adapter.ToDoAdapter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_todo_list.*
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class ToDoListFragment : Fragment(),
    TodoContract.View,
    CoroutineScope,
    DatePickerDialog.OnDateSetListener {

    override lateinit var presenter: TodoContract.Presenter

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var listener: OnFragmentInteractionListener? = null

    override lateinit var todoAdapter: ToDoAdapter

    private lateinit var menu: Menu

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
            val picker = presenter.createDatePicker()
            context?.let { picker.setCancelColor(ContextCompat.getColor(it, R.color.darkGray)) }
            picker.dismissOnPause(true)
            picker.setTitle("Select Date!!☆彡")
            activity?.supportFragmentManager?.let { picker.show(it, "Datepickerdialog") }
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        presenter.datePickerSet(year, monthOfYear, dayOfMonth)
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

        presenter = inject<TodoContract.Presenter>(
            "", null
        ) {
            parametersOf(
                this,
                inject<ToDoRepository>().value,
                inject<TagRepository>().value
            )
        }.value
        presenter.start()

        (activity as MainActivity).setActionBarTitle(presenter.titleDate.toStringSlash_yyyyMMdd())

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(view.context)

        todoAdapter = createToDoAdapter()
        todoAdapter.setHasStableIds(true)
        recycleView.adapter = todoAdapter
        notifyToDoChanged()

        // 区切り線の表示
        recycleView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )
        fab.setOnSafeClickListener {
            presenter.addNewToDo(it)
        }

        // 枠をタッチしたときにキーボードを閉じる
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
                    presenter.moveToDo(toPosition, fromPosition)
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

    private fun createToDoAdapter(): ToDoAdapter {
        return object : ToDoAdapter(presenter) {
            override fun onClick(v: View?, holder: ToDoViewHolder) {
                if (holder.isShowDetail) {
                    title.requestFocus()
                } else {
                    closeKeyboard(v)
                }
            }

            override fun onDoneCheck(v: CompoundButton?, holder: ToDoViewHolder) {
                launch(Dispatchers.IO) {
                    presenter.fixToDo(holder)
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
                    launch(Dispatchers.IO) {
                        presenter.fixToDo(holder)
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
                presenter.changeTag(v, holder)
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
                presenter.deleteToDo(holder)
            }

            override fun showContentDetail(v: View?, holder: ToDoViewHolder) {
                holder.tagTextView.visibility = View.VISIBLE
                holder.tagSpinner.visibility = View.VISIBLE
                holder.content.fullText = presenter.getMemo(holder.adapterPosition)
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
                holder.content.fullText = presenter.getMemo(holder.adapterPosition)
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
                    launch(job + Dispatchers.IO) {
                        Logger.d("in IO onContentFocusChange ")
                        presenter.fixToDo(holder)
                    }
                    closeKeyboard(v)
                    return true
                }
            }
        }
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
            presenter.saveToDoList()
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

    override fun tagDraw(v: View?, tag: Tag, holder: ToDoAdapter.ToDoViewHolder) {
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }


    override fun SetActionBarTitle(title: String) {
        (activity as MainActivity).setActionBarTitle(title)
    }

    override  fun notifyToDoChanged() {
        todoAdapter.notifyDataSetChanged()
    }

    override fun notifyToDoDelete(pos: Int) {
        todoAdapter.notifyItemRemoved(pos)
    }

    override fun notifyToDoAdd(){
        todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
    }
}

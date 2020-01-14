package com.folta.todoapp.todo

import android.content.Context
import android.graphics.Shader
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.utility.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.*
import com.folta.todoapp.utility.toStringSlashyyyyMMdd
import com.folta.todoapp.MainActivity
import com.folta.todoapp.utility.TileDrawable
import com.folta.todoapp.utility.setOnSafeClickListener
import com.folta.todoapp.todo.adapter.ToDoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

    /**
     * Fragmentのoverride 上部オプションメニュー作成
     *
     * @param menu
     * @param inflater
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.menu_todo, menu)
    }

    /**
     * Fragmentのoverride 上部オプションメニュー選択イベント
     *
     * @param item
     * @return
     */
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

    /**
     * オプションメニュー　削除クリック
     *
     * @param menuItem
     */
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

    /**
     * オプションメニュー　カレンダークリック
     *
     */
    private fun onClickCalendarOptionMenu() {
        launch(Dispatchers.IO) {
            val picker = presenter.createDatePicker()
            context?.let { picker.setCancelColor(ContextCompat.getColor(it, R.color.darkGray)) }
            picker.dismissOnPause(true)
            picker.setTitle("Select Date!!☆彡")
            activity?.supportFragmentManager?.let { picker.show(it, "Datepickerdialog") }
        }
    }

    /**
     * DatePickerDialogのoverride 日付選択後イベント
     *
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        presenter.datePickerSet(year, monthOfYear, dayOfMonth)
    }

    /**
     * Fragmentのoverride
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    /**
     * Fragmentのoverride
     *
     * @param view
     * @param savedInstanceState
     */
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

        (activity as MainActivity).setActionBarTitle(presenter.titleDate.toStringSlashyyyyMMdd())

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(view.context)

        todoAdapter = ToDoAdapter(this, presenter)
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
            onClickFab(it)
        }

        // 枠をタッチしたときにキーボードを閉じる
        coordinatorLayout.setOnTouchListener { v, _ ->
            return@setOnTouchListener onClickCoordinatorLayout(v)
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
                    return onMoveToDo(viewHolder, target)
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

    /**
     * ToDoをドラッグ移動させた時のイベント
     *
     * @param viewHolder
     * @param target
     * @return
     */
    private fun onMoveToDo(
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        presenter.moveToDo(toPosition, fromPosition)
        todoAdapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * Fabをクリックしたときのイベント
     *
     * @param fab
     */
    private fun onClickFab(fab: FloatingActionButton) {
        presenter.addNewToDo(fab)
    }

    internal fun onClickMemo(
        v: View?,
        holder: ToDoAdapter.ToDoViewHolder
    ) {
        Logger.d("onContentClick")
        openKeyboard(v)
    }

    internal fun onClickToDoTitle(v: View?, holder: ToDoAdapter.ToDoViewHolder) {
        openKeyboard(v)
    }

    internal fun onFocusChangeToDoMemo(
        v: View?,
        hasFocus: Boolean,
        holder: ToDoAdapter.ToDoViewHolder
    ): Boolean {
        if (hasFocus) {
            return holder.content.performClick()
        } else {
            holder.content.fullText = holder.content.text.toString()
            Logger.d("onContentFocusChange : $hasFocus")
            launch(job + Dispatchers.IO) {
                Logger.d("in IO onContentFocusChange ")
                todoAdapter.presenter.fixToDo(holder)
            }
            closeKeyboard(v)
            return true
        }
    }

    internal fun onClickToDoDeleteButton(v: View?, holder: ToDoAdapter.ToDoViewHolder) {
        presenter.deleteToDo(holder)
    }

    internal fun onClickToDoDetailButton(
        v: View?,
        holder: ToDoAdapter.ToDoViewHolder
    ) {
        if (todoAdapter.state == ToDoAdapter.ListShowState.DELETE) {
            onClickToDoDeleteButton(v, holder)
            return
        }
        if (holder.isShowDetail) {
            closeContentDetail(v, holder)
        } else {
            showContentDetail(v, holder)
        }
    }

    internal fun onSpinnerSelectedToDoTag(
        v: View?,
        holder: ToDoAdapter.ToDoViewHolder
    ) {
        presenter.changeTag(v, holder)
    }

    internal fun onEditorDoneToDoTitle(
        v: TextView?,
        holder: ToDoAdapter.ToDoViewHolder
    ): Boolean {
        closeKeyboard(v)
        return true
    }

    internal fun onFocusChangeToDoTitle(
        v: View?,
        hasFocus: Boolean,
        holder: ToDoAdapter.ToDoViewHolder
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

    internal fun onCheckToDoDone(
        v: View?,
        holder: ToDoAdapter.ToDoViewHolder
    ) {
        launch(Dispatchers.IO) {
            presenter.fixToDo(holder)
        }
    }

    private fun showContentDetail(v: View?, holder: ToDoAdapter.ToDoViewHolder) {
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

    private fun closeContentDetail(v: View?, holder: ToDoAdapter.ToDoViewHolder) {
        onFocusChangeToDoMemo(v, false, holder)

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

    internal fun onClickToDo(
        v: View?,
        holder: ToDoAdapter.ToDoViewHolder
    ) {
        if (holder.isShowDetail) {
            title.requestFocus()
        } else {
            closeKeyboard(v)
        }
    }

    private fun onClickCoordinatorLayout(v: View?): Boolean {
        closeKeyboard(v)
        return true
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

    override fun setActionBarTitle(title: String) {
        (activity as MainActivity).setActionBarTitle(title)
    }

    override fun notifyToDoChanged() {
        todoAdapter.notifyDataSetChanged()
    }

    override fun notifyToDoDelete(pos: Int) {
        todoAdapter.notifyItemRemoved(pos)
    }

    override fun notifyToDoAdd() {
        todoAdapter.notifyItemInserted(todoAdapter.itemCount - 1)
    }
}

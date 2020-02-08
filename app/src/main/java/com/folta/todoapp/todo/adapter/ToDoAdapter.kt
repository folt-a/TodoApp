package com.folta.todoapp.todo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.R
import com.folta.todoapp.todo.ToDoListFragment
import com.folta.todoapp.todo.TodoContract
import com.folta.todoapp.utility.Logger
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.android.synthetic.main.holder_todo.view.*

open class ToDoAdapter(
    val fragment: ToDoListFragment,
    val presenter: TodoContract.Presenter,
    private val tagSpinnerAdapter: TagSpinnerAdapter
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    enum class ListShowState {
        NORMAL,
        DELETE;
    }

    var state: ListShowState =
        ListShowState.NORMAL

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        when (state) {
            ListShowState.DELETE -> holder.bindDelete()
            ListShowState.NORMAL -> holder.bindNormal()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_todo, parent, false)
        val holder = ToDoViewHolder(view)

//        Spinnerアダプターセット
        holder.tagSpinner.adapter = tagSpinnerAdapter

//        listenerをセットする
        with(holder.itemView) {
            setOnClickListener { v ->
                fragment.onClickToDo(v, holder)
            }

            title.setOnClickListener { v ->
                fragment.onClickToDoTitle(v, holder)
            }

            title.setOnEditorActionListener { v, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE,
                    EditorInfo.IME_ACTION_NEXT -> {
                        fragment.onEditorDoneToDoTitle(v, holder)
                    }
                    else -> {
                        Logger.d(actionId.toString())
                        return@setOnEditorActionListener true
                    }
                }
            }

            title.setOnFocusChangeListener { v, hasFocus ->
                fragment.onFocusChangeToDoTitle(v, hasFocus, holder)
            }

            content.setOnClickListener { v ->
                fragment.onClickMemo(v, holder)
            }

            content.setOnFocusChangeListener { v, hasFocus ->
                fragment.onFocusChangeToDoMemo(v, hasFocus, holder)
            }

            detail.setOnClickListener { v ->
                fragment.onClickToDoDetailButton(v, holder)
            }
        }

        return holder
    }

    override fun getItemCount(): Int {
        return presenter.getToDoListSize()
    }

    override fun getItemId(position: Int): Long {
        return presenter.getToDoId(position)
    }

    inner class ToDoViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        var isShowDetail = false

        fun bindNormal() {
            isShowDetail = false

            fragment.tagDraw(containerView,presenter.getTagByToDoPos(this.adapterPosition),this)

            title.isEnabled = true
            title.setText(presenter.getTitle(this.adapterPosition))

            tagSpinner.onItemSelectedListener = null
            tagSpinner.setSelection(presenter.getTagNoByToDoPos(this.adapterPosition), false)

            tagSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    //Spinnerのドロップダウンアイテムが選択された時
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        fragment.onSpinnerSelectedToDoTag(view, this@ToDoViewHolder)
                    }

                    //Spinnerのドロップダウンアイテムが選択されなかった時
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            content.fullText = presenter.getMemo(this.adapterPosition)
            content.closeMemo()
            detail.cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.dp40)
            detail.setIconResource(R.drawable.ic_detail)
            detail.setIconTintResource(R.color.colorPrimaryDark)
            isDone.isEnabled = true
            isDone.setOnCheckedChangeListener { v, _ ->
                fragment.onCheckToDoDone(v, this)
            }
            isDone.isChecked = presenter.getChecked(this.adapterPosition)
        }

        fun bindDelete() {
            isShowDetail = false
            fragment.tagDraw(containerView,presenter.getTagByToDoPos(this.adapterPosition),this)
            title.isEnabled = false
            title.setText(presenter.getTitle(this.adapterPosition))
            tagTextView.visibility = View.GONE
            tagSpinner.visibility = View.GONE
            content.fullText = presenter.getMemo(this.adapterPosition)
            content.closeMemo()
            detail.cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.dp12)
            detail.setIconResource(R.drawable.ic_trash)
            detail.setIconTintResource(R.color.alert)
            isDone.isEnabled = false
            isDone.isChecked = presenter.getChecked(this.adapterPosition)
        }
    }
}
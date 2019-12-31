package com.folta.todoapp.view.ui.todo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.R
import com.folta.todoapp.view.ui.todo.TodoContract
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_todo.view.*

open class ToDoAdapter(
    val presenter: TodoContract.Presenter
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

    //    tagSpinnerAdapterのレイアウトは全てのToDoで共通なのでToDoAdapter生成時に固定
    private val tagSpinnerAdapter: TagSpinnerAdapter =
        TagSpinnerAdapter(presenter)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_todo, parent, false)
        val holder = ToDoViewHolder(view)

//        Spinnerアダプターセット
        holder.tagSpinner.adapter = tagSpinnerAdapter

//        listenerをセットする
        with(holder.itemView) {
            setOnClickListener { v ->
                onClick(v, holder)
            }

            title.setOnClickListener { v ->
                onTitleClick(v, holder)
            }

            title.setOnEditorActionListener { v, actionId, _ ->
                onTitleEditorAction(v, actionId, holder)
            }

            title.setOnFocusChangeListener { v, hasFocus ->
                if (onTitleFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
            }

            content.setOnClickListener { v ->
                onContentClick(v, holder)
            }

            content.setOnFocusChangeListener { v, hasFocus ->
                if (onContentFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
            }

            detail.setOnClickListener { v ->
                onClickDetail(v, holder)
            }
        }

        return holder
    }

    open fun onContentClick(v: View?, holder: ToDoViewHolder) {
    }

    override fun getItemCount(): Int {
        return presenter.getToDoListSize()
    }

    override fun getItemId(position: Int): Long {
        return presenter.getToDoId(position)
    }

    open fun onTitleEditorAction(v: TextView?, actionId: Int, holder: ToDoViewHolder): Boolean {
        return true
    }

    open fun onTitleFocusChange(v: View?, hasFocus: Boolean, holder: ToDoViewHolder): Boolean {
        return true
    }

    open fun onDoneCheck(v: CompoundButton?, holder: ToDoViewHolder) {
    }

    open fun onContentFocusChange(v: View?, hasFocus: Boolean, holder: ToDoViewHolder): Boolean {
        return true
    }

    open fun onClick(v: View?, holder: ToDoViewHolder) {
    }

    open fun onTitleClick(v: View?, holder: ToDoViewHolder) {
    }

    open fun onSpinnerSelected(v: View?, id: Int, holder: ToDoViewHolder) {
    }

    open fun onClickDetail(v: View?, holder: ToDoViewHolder) {
    }

    open fun onClickDelete(v: View?, holder: ToDoViewHolder) {
    }

    open fun showContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    open fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    inner class ToDoViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        var isShowDetail = false

        fun bindNormal() {
            isShowDetail = false

            presenter.changeTag(containerView,this)

            title.isEnabled = true
            title.setText(presenter.getTitle(this.adapterPosition))
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
                        onSpinnerSelected(view, id.toInt(), this@ToDoViewHolder)
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
                onDoneCheck(v, this)
            }
            isDone.isChecked = presenter.getChecked(this.adapterPosition)
        }

        fun bindDelete() {
            isShowDetail = false
            presenter.changeTag(containerView,this)
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
package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_todo.view.*

open class ToDoAdapter(
    var items: List<ToDo>,
    private val tagList: MutableList<Tag>
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    enum class ListShowState {
        NORMAL,
        DELETE;
    }

    var state: ListShowState = ListShowState.NORMAL

    init {
        tagList.add(
            0,
            Tag(id = 0, tagName = "タグなし", pattern = R.drawable.bg_pattern1, color = R.color.white)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        val item = items[pos]
        when (state) {
            ListShowState.DELETE -> holder.bindDelete(item, tagList)
            ListShowState.NORMAL -> holder.bindNormal(item, tagList)
        }

    }

    //    tagSpinnerAdapterのレイアウトは全てのToDoで共通なのでToDoAdapter生成時に固定
    private val tagSpinnerAdapter: TagSpinnerAdapter = TagSpinnerAdapter(tagList)

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
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }


    internal fun getEditedToDo(holder: ToDoViewHolder): ToDo? {
        val item = items.getOrNull(holder.adapterPosition)
        item?.title = holder.title.text.toString()
        item?.tagId = holder.tagSpinner.selectedItemId.toInt()
        if (holder.content.isEnabled) {
            item?.content = holder.content.fullText
            Logger.d("content change : " + holder.content.fullText)
        }
        item?.isChecked = holder.isDone.isChecked
        return item
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

        fun bindNormal(todo: ToDo, tagList: List<Tag>) {
            isShowDetail = false
            // todoTag
            var tag = tagList.firstOrNull { it.id == todo.tagId }
//        タグなし、削除済みタグは未設定タグとして描画する
            if (tag == null || tag.isDeleted) {
                tag = tagList[0]
            }
            val colorResId = tag.color
            val patternResId = tag.pattern
            val drawable = TileDrawable.create(
                todoTag.context,
                colorResId,
                patternResId,
                Shader.TileMode.REPEAT
            )
            todoTag.setImageDrawable(drawable)
            title.isEnabled = true
            title.setText(todo.title)
            tagSpinner.setSelection(tagList.indexOf(tag), false)
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
            content.fullText = todo.content
            content.closeMemo()
            detail.cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.dp40)
            detail.setIconResource(R.drawable.ic_detail)
            detail.setIconTintResource(R.color.colorPrimaryDark)
            isDone.isEnabled = true
            isDone.setOnCheckedChangeListener { v, _ ->
                onDoneCheck(v, this)
            }
            isDone.isChecked = todo.isChecked
        }

        fun bindDelete(todo: ToDo, tagList: List<Tag>) {
            isShowDetail = false
            // todoTag
            var tag = tagList.firstOrNull { it.id == todo.tagId }
            //  タグなし、削除済みタグは未設定タグとして描画する
            if (tag == null || tag.isDeleted) {
                tag = tagList[0]
            }
            val colorResId = tag.color
            val patternResId = tag.pattern
            val drawable = TileDrawable.create(todoTag.context, colorResId, patternResId, Shader.TileMode.REPEAT)
            todoTag.setImageDrawable(drawable)
            title.isEnabled = false
            title.setText(todo.title)
            tagTextView.visibility = View.GONE
            tagSpinner.visibility = View.GONE
            content.fullText = todo.content
            content.closeMemo()
            detail.cornerRadius = itemView.context.resources.getDimensionPixelSize(R.dimen.dp12)
            detail.setIconResource(R.drawable.ic_trash)
            detail.setIconTintResource(R.color.alert)
            isDone.isEnabled = false
            isDone.isChecked = todo.isChecked
        }
    }
}
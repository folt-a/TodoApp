package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.synthetic.main.holder_todo.view.*

open class ToDoAdapter(var items: List<ToDo>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
//        タグの背景を設定
        val drawable = ContextCompat.getDrawable(holder.content.context, R.drawable.bg_pattern2)
        drawable?.let {
            holder.todoTagColor.setImageDrawable(TileDrawable(it, Shader.TileMode.REPEAT))
        }

        holder.title.setText(items[pos].title)
//      本文は改行削除＋入らない部分は非表示にする
        holder.contentText = items[pos].content
        if (holder.contentText.length > 20) {
            holder.content.setText("${holder.contentText.replace("\n", " ").substring(0..20)}...")
            holder.content.visibility = View.VISIBLE
        } else if (items[pos].content.isEmpty()) {
            holder.content.visibility = View.GONE
        } else {
            holder.content.visibility = View.VISIBLE
            holder.content.setText(holder.contentText.replace("\n", " "))
        }
        holder.isDone.isChecked = items[pos].isChecked
        Logger.d(holder.contentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_todo, parent, false)
        val holder = ToDoViewHolder(view)

        holder.itemView.setOnClickListener { v ->
            onClick(v, holder)
        }

        holder.itemView.isDone.setOnCheckedChangeListener { v, _ ->
            onDoneCheck(v, holder)
        }

        holder.itemView.title.setOnClickListener { v ->
            onTitleClick(v, holder)
        }

        holder.itemView.title.setOnEditorActionListener { v, actionId, _ ->
            onTitleEditorAction(v, actionId, holder)
        }

        holder.itemView.title.setOnFocusChangeListener { v, hasFocus ->
            if (onTitleFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
        }

        holder.itemView.content.setOnClickListener { v ->
            onContentClick(v, holder)
        }

        holder.itemView.content.setOnFocusChangeListener { v, hasFocus ->
            if (onContentFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
        }

        holder.itemView.detail.setOnClickListener { v ->
            if (holder.isShowDetail) {
                closeContentDetail(v, holder)
            } else {
                showContentDetail(v, holder)
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
        if (holder.content.isEnabled) {
            item?.content = holder.contentText
            Logger.d("content change : " + holder.contentText)
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

    open fun showContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    open fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    class ToDoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val inputMethodManager =
            getSystemService(itemView.context, InputMethodManager::class.java)
        val linearLayout: LinearLayout = itemView.linearLayout
        val todoTagColor: ImageView = itemView.todoTagColor
        val title: EditText = itemView.title
        val content: EditText = itemView.content
        var contentText: String = ""
        val isDone: CheckBox = itemView.isDone
        val detail: ImageButton = itemView.detail
        var isShowDetail = false
    }
}
package com.folta.todoapp.view

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.folta.todoapp.R
import com.folta.todoapp.data.local.ToDo
import kotlinx.android.synthetic.main.holder_todo.view.*
import androidx.core.content.ContextCompat.getSystemService
import com.folta.todoapp.Logger


open class ToDoAdapter(var items: List<ToDo>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        holder.title.setText(items[pos].id.toString())
//      本文は改行削除＋入らない部分は非表示にする
        if (items[pos].content.length > 20) {
            holder.content.text = items[pos].content.replace("\n", " ").substring(0..20)
        }
        holder.isDone.isChecked = items[pos].isChecked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_todo, parent, false)

        return ToDoViewHolder(view, this)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun onDetailClick(holder: ToDoViewHolder, pos: Int) {
//        contentを全文表示する
        holder.content.text = items[pos].content
        holder.detail.setImageResource(R.drawable.ic_detail_selected)
    }

    private fun onFocusOut(holder: ToDoViewHolder, pos: Int) {
//      本文は改行削除＋入らない部分は非表示にする
        if (items[pos].content.length > 20) {
            holder.content.text = items[pos].content.replace("\n", " ").substring(0..20)
        }
        holder.detail.setImageResource(R.drawable.ic_detail)
    }

    private fun onTitleClick(holder: ToDoViewHolder) {
        holder.title.isFocusable = true
        holder.title.isFocusableInTouchMode = true
        holder.title.requestFocus()
    }

    private fun onTitleFocusOut(holder: ToDoViewHolder) {
        holder.title.isFocusable = false
        holder.title.isFocusableInTouchMode = false
    }

    class ToDoViewHolder(itemView: View, private val adapter: ToDoAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private val inputMethodManager =
            getSystemService(itemView.context, InputMethodManager::class.java)
        private val linearLayout:LinearLayout = itemView.linearLayout
        val title: EditText = itemView.title
        val content: TextView = itemView.content
        val isDone: CheckBox = itemView.isDone
        val detail:ImageButton = itemView.detail
        private var isShowDetail = false

        init {
            itemView.title.setOnClickListener { v ->
                adapter.onTitleClick(this)
                inputMethodManager?.showSoftInput(v, 1)
            }
            itemView.title.setOnEditorActionListener { v, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        adapter.onTitleFocusOut(this)
                        inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            itemView.title.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener
                adapter.onTitleFocusOut(this)
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            itemView.detail.setOnClickListener { v ->
                Logger.d(isShowDetail.toString())
                if (isShowDetail){
                    adapter.onFocusOut(this, this.adapterPosition)
                }else{
                    adapter.onDetailClick(this,this.adapterPosition)
                }
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
                isShowDetail = !isShowDetail
            }
            itemView.setOnClickListener { v ->
                linearLayout.requestFocus()
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            }
//            itemView.linearLayout.setOnFocusChangeListener { v, hasFocus ->
//                if (!hasFocus) return@setOnFocusChangeListener
//                Logger.d("focus out!!!")
//                isShowDetail = false
//                adapter.onFocusOut(this, this.adapterPosition)
//                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
//            }
        }
    }
}
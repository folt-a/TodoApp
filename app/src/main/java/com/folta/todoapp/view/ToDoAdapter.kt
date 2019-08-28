package com.folta.todoapp.view

import android.annotation.SuppressLint
import android.util.TypedValue
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
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.folta.todoapp.Logger


open class ToDoAdapter(var items: List<ToDo>) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        holder.title.setText(items[pos].id.toString())
//      本文は改行削除＋入らない部分は非表示にする
        if (items[pos].content.length > 20) {
            holder.content.setText("${items[pos].content.replace("\n", " ").substring(0..20)}...")
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
        holder.content.setText(items[pos].content)
    }

    @SuppressLint("SetTextI18n")
    private fun onFocusOut(holder: ToDoViewHolder, pos: Int) {
//      本文は改行削除＋入らない部分は非表示にする
        if (items[pos].content.length > 20) {
            holder.content.setText("${items[pos].content.replace("\n", " ").substring(0..20)}...")
        }
    }

    class ToDoViewHolder(itemView: View, private val adapter: ToDoAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private val inputMethodManager =
            getSystemService(itemView.context, InputMethodManager::class.java)
        private val linearLayout: LinearLayout = itemView.linearLayout
        val title: EditText = itemView.title
        val content: EditText = itemView.content
        val isDone: CheckBox = itemView.isDone
        private val detail: ImageButton = itemView.detail
        private var isShowDetail = false

        init {
            itemView.title.setOnClickListener { v ->
                title.isFocusable = true
                title.isFocusableInTouchMode = true
                title.requestFocus()
                inputMethodManager?.showSoftInput(v, 1)
            }
            itemView.title.setOnEditorActionListener { v, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        title.isFocusable = false
                        title.isFocusableInTouchMode = false
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
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            }

            itemView.detail.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) return@setOnFocusChangeListener
                content.isEnabled = false
                content.isFocusable = false
                content.isFocusableInTouchMode = false
                content.background = null
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            itemView.detail.setOnClickListener { v ->
                Logger.d(isShowDetail.toString())
                if (isShowDetail) {
                    adapter.onFocusOut(this, adapterPosition)
                    content.background = null
                    content.setPadding(
                        0,
                        0,
                        0,
                        0
                    )
                    content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    val mlp = content.layoutParams
                    if (mlp is ViewGroup.MarginLayoutParams) mlp.setMargins(0)
                    detail.setImageResource(R.drawable.ic_detail)
                } else {
                    adapter.onDetailClick(this, adapterPosition)
                    content.setBackgroundResource(R.drawable.edittext_content)
                    content.setPadding(
                        v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                        v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                        v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                        v.context.resources.getDimensionPixelSize(R.dimen.dp8)
                    )
                    content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    val mlp = content.layoutParams
                    if (mlp is ViewGroup.MarginLayoutParams) {
                        mlp.setMargins(
                            0,
                            v.context.resources.getDimensionPixelSize(R.dimen.dp8),
                            0,
                            0
                        )
                    }

                    detail.setImageResource(R.drawable.ic_detail_selected)
                }

                content.isEnabled = !content.isEnabled
                content.isFocusable = !content.isFocusable
                content.isFocusableInTouchMode = !content.isFocusableInTouchMode

                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
                isShowDetail = !isShowDetail
            }
            itemView.setOnClickListener { v ->
                linearLayout.requestFocus()
                inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
            }

        }
    }
}
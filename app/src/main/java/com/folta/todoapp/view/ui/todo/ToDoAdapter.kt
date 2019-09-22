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
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.synthetic.main.holder_todo.view.*
import android.widget.AdapterView
import android.widget.Toast
import android.widget.Spinner
import android.widget.AdapterView.OnItemSelectedListener


open class ToDoAdapter(
    var items: List<ToDo>,
    private val tagList: MutableList<Tag>
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    init {
        tagList.add(
            0,
            Tag(id = 0, tagName = "タグなし", pattern = R.drawable.bg_pattern1, color = R.color.white)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        val item = items[pos]

//        todoTag
        var tag = tagList.firstOrNull { it.id == item.tagId }
//        タグなし、削除済みタグは未設定タグとして描画する
        if (tag == null || tag.isDeleted) {
            tag = tagList[0]
        }
        val colorResId = tag.color
        val patternResId = tag.pattern
        val drawable = ContextCompat.getDrawable(holder.todoTag.context, patternResId)
        drawable?.let {
            drawable.setTint(holder.todoTag.resources.getColor(colorResId))
            holder.todoTag.setImageDrawable(TileDrawable(it, Shader.TileMode.REPEAT))
        }
        holder.tagSpinner.setSelection(tagList.indexOf(tag))
        holder.title.setText(item.title)
        holder.content.setMemoText(item.content)
        holder.isDone.isChecked = item.isChecked
        Logger.d(holder.content.fullText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_todo, parent, false)
        val holder = ToDoViewHolder(view)

//        Spinner
        val spinnerTagAdapter = TagSpinnerAdapter(tagList)
        holder.tagSpinner.adapter = spinnerTagAdapter
        spinnerTagAdapter.notifyDataSetChanged()

//        listenerをセットする
        with(holder.itemView) {
            setOnClickListener { v ->
                onClick(v, holder)
            }

            isDone.setOnCheckedChangeListener { v, _ ->
                onDoneCheck(v, holder)
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

            tagSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                //Spinnerのドロップダウンアイテムが選択された時
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onSpinnerSelected(view, id.toInt(), holder)
                }

                //Spinnerのドロップダウンアイテムが選択されなかった時
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            content.setOnClickListener { v ->
                onContentClick(v, holder)
            }

            content.setOnFocusChangeListener { v, hasFocus ->
                if (onContentFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
            }

            detail.setOnClickListener { v ->
                if (holder.isShowDetail) {
                    closeContentDetail(v, holder)
                } else {
                    showContentDetail(v, holder)
                }
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

    open fun showContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    open fun closeContentDetail(v: View?, holder: ToDoViewHolder) {
    }

    class ToDoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val inputMethodManager =
            getSystemService(itemView.context, InputMethodManager::class.java)
        val linearLayout: LinearLayout = itemView.linearLayout
        val todoTag: ImageView = itemView.todoTag
        val title: EditText = itemView.title
        val content: EditTextMemo = itemView.content
        val tagSpinner: Spinner = itemView.tagSpinner
        val isDone: CheckBox = itemView.isDone
        val detail: ImageButton = itemView.detail
        var isShowDetail = false


    }
}
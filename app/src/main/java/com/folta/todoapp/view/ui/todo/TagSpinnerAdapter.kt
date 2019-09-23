package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import kotlinx.android.synthetic.main.tag_spinner_item.view.*
import kotlinx.android.synthetic.main.tag_spinner_selected_item.view.*

class TagSpinnerAdapter(private val items: List<Tag>) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val itemView = layoutInflater.inflate(R.layout.tag_spinner_selected_item, null)
        itemView.tagName_selected.text = items[position].tagName
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val itemView = layoutInflater.inflate(R.layout.tag_spinner_item, null)
        val holder = TagSpinnerViewHolder(itemView)

        val tag: Tag = items[position]
        if (tag.id == 0) {
            val tagPatternDrawable =
                ContextCompat.getDrawable(holder.todoTag.context, R.drawable.bg_pattern1)?.mutate()
            tagPatternDrawable?.let { it ->
                it.setTint(ContextCompat.getColor(holder.todoTag.context, R.color.white))
                holder.todoTag.setImageDrawable(it)
            }
        } else {
            val tagPatternDrawable = ContextCompat.getDrawable(holder.todoTag.context, tag.pattern)?.mutate()
            tagPatternDrawable?.let { it ->
                it.setTint(ContextCompat.getColor(holder.todoTag.context, tag.color))
                holder.todoTag.setImageDrawable(it)
            }
        }
        holder.tagName.text = tag.tagName
        return itemView
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    class TagSpinnerViewHolder(itemView: View) {
        val todoTag: ImageView = itemView.todoTag
        val tagName: TextView = itemView.tagName
    }
}
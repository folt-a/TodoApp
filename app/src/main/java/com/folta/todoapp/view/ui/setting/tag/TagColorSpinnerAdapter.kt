package com.folta.todoapp.view.ui.setting.tag

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.folta.todoapp.R
import kotlinx.android.synthetic.main.tag_color_spinner_item.view.*

class TagColorSpinnerAdapter(private val items: Array<Int>) : BaseAdapter() {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView: View
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            itemView = layoutInflater.inflate(R.layout.tag_color_spinner_item, parent, false)
        } else {
            itemView = convertView
        }
        val colorId: Int = items[position]
        val tagPatternDrawable =
            ContextCompat.getDrawable(itemView.tagColor.context, R.drawable.bg_pattern1)?.mutate()
        tagPatternDrawable?.let { it ->
            it.setTint(ContextCompat.getColor(itemView.tagColor.context, colorId))
            itemView.tagColor.setImageDrawable(it)
        }
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }
}
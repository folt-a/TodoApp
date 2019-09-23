package com.folta.todoapp.view.ui.setting.tag

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import kotlinx.android.synthetic.main.tag_pattern_spinner_item.view.*

class TagPatternSpinnerAdapter(private val items: Array<Int>) : BaseAdapter() {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView: View
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            itemView = layoutInflater.inflate(R.layout.tag_pattern_spinner_item, parent, false)
        } else {
            itemView = convertView
        }

        val patternId: Int = items[position]
        val tagPatternDrawable =
            ContextCompat.getDrawable(itemView.tagPattern.context, patternId)?.mutate()
        tagPatternDrawable?.let { it ->
            it.setTint(ContextCompat.getColor(itemView.tagPattern.context, R.color.black))
            itemView.tagPattern.setImageDrawable(it)
        }
        return itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }

    override fun getItemId(position: Int): Long {
//        Logger.d("TagPatternSpinnerAdapter getItemId : ${items[position]}")
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.count()
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }
}
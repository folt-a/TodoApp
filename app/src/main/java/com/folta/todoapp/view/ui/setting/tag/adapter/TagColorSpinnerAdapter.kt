package com.folta.todoapp.view.ui.setting.tag.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.folta.todoapp.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.tag_color_spinner_item.*

class TagColorSpinnerAdapter(private val items: Array<Int>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TagColorSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = TagColorSpinnerViewHolder(
                layoutInflater.inflate(R.layout.tag_color_spinner_item, parent, false)
            )
        } else {
            return convertView
        }
        val colorId: Int = items[position]
        holder.bind(colorId)
        return holder.containerView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TagColorSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = TagColorSpinnerViewHolder(
                layoutInflater.inflate(
                    R.layout.tag_color_spinner_item,
                    parent,
                    false
                )
            )
        } else {
            return convertView
        }
        val colorId: Int = items[position]
        holder.bind(colorId)
        return holder.containerView
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

    inner class TagColorSpinnerViewHolder(override val containerView: View) :
        LayoutContainer {
        fun bind(colorId: Int) {
            val tagPatternDrawable =
                ContextCompat.getDrawable(tagColor.context, R.drawable.bg_pattern1)?.mutate()
            tagPatternDrawable?.let { it ->
                it.setTint(ContextCompat.getColor(tagColor.context, colorId))
                tagColor.setImageDrawable(it)
            }
        }
    }
}
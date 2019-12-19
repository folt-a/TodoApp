package com.folta.todoapp.view.ui.setting.tag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.folta.todoapp.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.tag_pattern_spinner_item.*

class TagPatternSpinnerAdapter(private val items: Array<Int>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TagPatternSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = TagPatternSpinnerViewHolder(layoutInflater.inflate(R.layout.tag_pattern_spinner_item, parent, false))
        } else {
            return convertView
        }
        val patternId: Int = items[position]
        holder.bind(patternId)
        return holder.containerView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TagPatternSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = TagPatternSpinnerViewHolder(layoutInflater.inflate(R.layout.tag_pattern_spinner_item, parent, false))
        } else {
            return convertView
        }
        val patternId: Int = items[position]
        holder.bind(patternId)
        return holder.containerView
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

    inner class TagPatternSpinnerViewHolder(override val containerView: View) :
        LayoutContainer {
        fun bind(patternId: Int) {
            val tagPatternDrawable =
                ContextCompat.getDrawable(tagPattern.context, patternId)?.mutate()
            tagPatternDrawable?.let { it ->
                it.setTint(ContextCompat.getColor(tagPattern.context, R.color.black))
                tagPattern.setImageDrawable(it)
            }
        }
    }
}
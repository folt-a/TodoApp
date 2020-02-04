package com.folta.todoapp.todo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.folta.todoapp.utility.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.todo.TodoContract
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.tag_spinner_item.*
import kotlinx.android.synthetic.main.tag_spinner_selected_item.*

class TagSpinnerAdapter(val presenter: TodoContract.Presenter) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: SelectedTagSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = SelectedTagSpinnerViewHolder(
                layoutInflater.inflate(
                    R.layout.tag_spinner_selected_item,
                    parent,
                    false
                )
            )
        } else {
            holder = SelectedTagSpinnerViewHolder(convertView)
        }
        Logger.i("getView POSITION:$position")
        val tag: Tag = presenter.getTagByTagPos(position)
        holder.bind(tag)
        return holder.containerView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: TagSpinnerViewHolder
        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent?.context)
            holder = TagSpinnerViewHolder(
                layoutInflater.inflate(
                    R.layout.tag_spinner_item,
                    parent,
                    false
                )
            )
        } else {
            holder = TagSpinnerViewHolder(convertView)
        }
        Logger.i("getDropDownView POSITION:$position")
        val tag: Tag = presenter.getTagByTagPos(position)
        holder.bind(tag)
        return holder.containerView
    }

    override fun getItemId(position: Int): Long {
        return presenter.getTagId(position)
    }

    override fun getCount(): Int {
        return presenter.getTagListSize()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItem(position: Int): Any {
        return presenter.getTagByTagPos(position)
    }

    inner class TagSpinnerViewHolder(override val containerView: View) : LayoutContainer {
        fun bind(tag: Tag) {
            if (tag.id == 0) {
                val tagPatternDrawable =
                    ContextCompat.getDrawable(spinnerTag.context, R.drawable.bg_pattern1)?.mutate()
                tagPatternDrawable?.let { it ->
                    it.setTint(ContextCompat.getColor(spinnerTag.context, R.color.white))
                    spinnerTag.setImageDrawable(it)
                }
            } else {
                val tagPatternDrawable =
                    ContextCompat.getDrawable(spinnerTag.context, tag.pattern)?.mutate()
                tagPatternDrawable?.let { it ->
                    it.setTint(ContextCompat.getColor(spinnerTag.context, tag.color))
                    spinnerTag.setImageDrawable(it)
                }
            }
            tagName.text = tag.tagName
        }
    }

    inner class SelectedTagSpinnerViewHolder(override val containerView: View) : LayoutContainer {
        fun bind(tag: Tag) {
            tagNameSelected.text = tag.tagName
        }
    }
}
package com.folta.todoapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.folta.todoapp.R
import com.folta.todoapp.data.local.ToDo
import kotlinx.android.synthetic.main.adapter_list_todo.view.*

class ToDoAdapter(context: Context) : ArrayAdapter<ToDo>(context, 0) {
    val mInflater = LayoutInflater.from(context)
    val packageManager = context.packageManager

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: mInflater.inflate(R.layout.adapter_list_todo, parent, false)
        val item = getItem(position)

        view.title.text = item.title
        view.content.text = item.content
        view.isDone.isChecked = item.isChecked


        return view
    }
}
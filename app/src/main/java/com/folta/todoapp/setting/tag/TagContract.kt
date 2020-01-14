package com.folta.todoapp.setting.tag

import android.view.View
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.setting.tag.adapter.TagAdapter
import com.folta.todoapp.todo.TodoContract
import com.folta.todoapp.utility.BasePresenter
import com.folta.todoapp.utility.BaseView
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface TagContract {
    interface View : BaseView<Presenter> {
        var tagAdapter: TagAdapter
        override var presenter: Presenter

        fun notifyTagChanged()
        fun notifyTagDelete(pos: Int)
        fun notifyTagAdd()
        fun tagDraw(v: android.view.View?, tag: Tag, holder: TagAdapter.TagViewHolder)
    }

    interface Presenter : BasePresenter {
        fun addNewTag(fab: FloatingActionButton?)

        fun getTagByPos(pos: Int): Tag
        suspend fun fixTag(holder: TagAdapter.TagViewHolder): Tag?
        fun deleteTag(pos: Int)
        fun getTagListSize(): Int
        fun getTagId(position: Int): Long
    }
}
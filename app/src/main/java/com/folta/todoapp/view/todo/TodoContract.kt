package com.folta.todoapp.view.todo

import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.view.BasePresenter
import com.folta.todoapp.view.BaseView
import com.folta.todoapp.view.todo.adapter.ToDoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import org.threeten.bp.LocalDate

interface TodoContract {
    interface View : BaseView<Presenter>, DatePickerDialog.OnDateSetListener {

        var todoAdapter: ToDoAdapter
        override var presenter: Presenter
        fun SetActionBarTitle(title: String)
        fun tagDraw(v: android.view.View?, tag: Tag, holder: ToDoAdapter.ToDoViewHolder)
        fun notifyToDoChanged()
        fun notifyToDoDelete(pos: Int)
        fun notifyToDoAdd()
    }
    interface Presenter :BasePresenter{
        var titleDate: LocalDate
        suspend fun createDatePicker(): DatePickerDialog
        fun datePickerSet(year: Int, monthOfYear: Int, dayOfMonth: Int)
        fun addNewToDo(fab:FloatingActionButton)
        fun moveToDo(toPosition: Int, fromPosition: Int)
        fun getToDoListSize(): Int
        fun getToDoId(position: Int): Long
        suspend fun fixToDo(holder: ToDoAdapter.ToDoViewHolder): ToDo?
        fun changeTag(v: android.view.View?, holder: ToDoAdapter.ToDoViewHolder)
        fun deleteToDo(holder: ToDoAdapter.ToDoViewHolder)
        fun getMemo(pos: Int): String
        fun saveToDoList()
        fun getTitle(pos: Int): String
        fun getChecked(pos: Int): Boolean
        fun getTagNoByToDoPos(pos: Int): Int
        fun getTagId(position: Int): Long
        fun getTagListSize(): Int
        fun getTagByTagPos(tagPos: Int): Tag
        fun getTagByToDoPos(pos: Int): Tag
    }
}
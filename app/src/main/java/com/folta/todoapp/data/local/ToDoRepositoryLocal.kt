package com.folta.todoapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.folta.todoapp.Const

class ToDoRepositoryLocal(context: Context) : ToDoRepository {
    private val dao: ToDoDAO

    init {
        val db = Room.databaseBuilder(context, MyDataBase::class.java, "todo").build()
        dao = db.todoDAO()
    }

    override suspend fun save(todo: ToDo): Int {
        dao.add(todo)
        return dao.getNewestId()
    }

    override suspend fun findByDate(dateyyMMDD: String): List<ToDo> {
        return dao.findByDate(dateyyMMDD)
    }

    override suspend fun delete(id: Int) {
        dao.delete(id)
    }

    override suspend fun find(Id: Int): ToDo {
        return dao.findById(Id)
    }

    override suspend fun saveSort(todos: Iterable<ToDo>) {
        todos.forEachIndexed { index, toDo ->
            toDo.orderId = 1 + index
        }
        dao.updateViewSort(todos)
    }

}
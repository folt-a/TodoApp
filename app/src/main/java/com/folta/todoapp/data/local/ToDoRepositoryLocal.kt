package com.folta.todoapp.data.local

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Job

class ToDoRepositoryLocal(context: Context) : ToDoRepository {
    private val job = Job()
    private val dao: ToDoDAO

    init {
        val db = Room.databaseBuilder(context, MyDataBase::class.java, "todo").build()
        dao = db.todoDAO()
    }

    override suspend fun save(todo: ToDo) {
        dao.add(todo)
    }

    override suspend fun findByDate(dateyyMMDD: String): List<ToDo> {
        return dao.findByDate(dateyyMMDD)
    }


}
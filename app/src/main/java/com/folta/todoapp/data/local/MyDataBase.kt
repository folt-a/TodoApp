package com.folta.todoapp.data.local

import androidx.room.*

@Database(entities = [ToDo::class,Tag::class], version = 4)
abstract class MyDataBase : RoomDatabase() {
    abstract fun todoDAO(): ToDoDAO
    abstract fun tagDAO(): TagDAO

    companion object {
        lateinit var db :MyDataBase
    }
}
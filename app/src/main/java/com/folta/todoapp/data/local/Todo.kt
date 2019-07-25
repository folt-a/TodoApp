package com.folta.todoapp.data.local

import androidx.room.*

@Database(entities = [ToDo::class], version = 2)
abstract class MyDataBase : RoomDatabase() {
    abstract fun todoDAO(): ToDoDAO
}

@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val isChecked: Boolean,
    val title: String,
    val content: String,
    val createdAt: String
)

@Dao
interface ToDoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(todo: ToDo)

    @Query("Select * from ToDo")
    suspend fun getAll(): List<ToDo>

    @Query("Select * from ToDo where createdAt = :dateyyyyMMDD ")
    suspend fun findByDate(dateyyyyMMDD: String): List<ToDo>

    @Update
    suspend fun update(todo: ToDo)

    @Delete
    suspend fun delete(todo: ToDo)
}
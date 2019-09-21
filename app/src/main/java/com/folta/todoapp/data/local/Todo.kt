package com.folta.todoapp.data.local

import androidx.room.*

@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var orderId: Int,
    var isChecked: Boolean,
    var title: String,
    var content: String,
    // yyyyMMdd
    val createdAt: String
)

@Dao
interface ToDoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(todo: ToDo)

    @Query("select * from ToDo order by orderId")
    suspend fun getAll(): List<ToDo>

    @Query("select * from ToDo where id = :id")
    suspend fun findById(id: Int): ToDo?

    @Query("select max(id) from ToDo")
    suspend fun getNewestId(): Int

    @Query("select * from ToDo where createdAt = :dateyyyyMMdd order by orderId")
    suspend fun findByDate(dateyyyyMMdd: String): List<ToDo>

    @Transaction
    suspend fun updateViewSort(todos: Iterable<ToDo>) {
        for (todo in todos) {
            updateOrderId(todo.id, todo.orderId)
        }
    }

    @Query("update ToDo set orderId = :orderId where id = :id")
    suspend fun updateOrderId(id: Int, orderId: Int)

    @Query("delete from ToDo where id = :id")
    suspend fun delete(id: Int)
}
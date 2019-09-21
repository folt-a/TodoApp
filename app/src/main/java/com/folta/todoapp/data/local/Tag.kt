package com.folta.todoapp.data.local

import androidx.room.*

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var tagName: String,
    var icon: Int,
    var color: Int,
    var isDeleted: Boolean = false
)

@Dao
interface TagDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: Tag)

    @Query("select max(id) from Tag")
    suspend fun getNewestId(): Int

    @Query("select * from Tag order by id")
    suspend fun getAll(): List<Tag>

    @Query("select * from Tag where id = :id")
    suspend fun findById(id: Int): Tag?

    @Query("select count(*) from Tag")
    suspend fun count(): Int

}
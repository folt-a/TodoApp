package com.folta.todoapp.data.local

import androidx.room.*

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var tagName: String,
    var pattern: Int,
    var color: Int,
    var isDeleted: Boolean = false
)

@Dao
interface TagDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: Tag)

    @Query("select max(id) from Tag where isDeleted = 0")
    suspend fun getNewestId(): Int

    @Query("select * from Tag where isDeleted = 0 order by id")
    suspend fun getAll(): List<Tag>

    @Query("select * from Tag where id = :id and isDeleted = 0")
    suspend fun findById(id: Int): Tag?

    @Query("select count(*) from Tag where isDeleted = 0")
    suspend fun count(): Int

}
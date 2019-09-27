package com.folta.todoapp.data.local

interface TagRepository {
    suspend fun init()
    suspend fun find(Id: Int): Tag?
    suspend fun getAll():List<Tag>
    suspend fun save(tag: Tag) : Int
    suspend fun delete(id: Int)
    suspend fun count():Int
}
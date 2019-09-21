package com.folta.todoapp.data.local

import com.folta.todoapp.R

class TagRepositoryLocal : TagRepository {
    private val dao: TagDAO = MyDataBase.db.tagDAO()

    override suspend fun init() {
        val tags = listOf<Tag>(
            Tag(
                id = 0,
                tagName = "",
                icon = R.drawable.bg_pattern2,
                color = R.color.colorAccent
            ),
            Tag(
                id = 0,
                tagName = "",
                icon = R.drawable.bg_pattern2,
                color = R.color.colorAccent
            ),
            Tag(
                id = 0,
                tagName = "",
                icon = R.drawable.bg_pattern2,
                color = R.color.colorAccent
            ),
            Tag(
                id = 0,
                tagName = "",
                icon = R.drawable.bg_pattern2,
                color = R.color.colorAccent
            ),
            Tag(
                id = 0,
                tagName = "",
                icon = R.drawable.bg_pattern2,
                color = R.color.colorAccent
            )
        )
        for (tag in tags) {
            dao.upsert(tag)
        }
    }

    override suspend fun find(Id: Int): Tag? {
        return dao.findById(Id)
    }

    override suspend fun getAll(): List<Tag> {
        return dao.getAll()
    }

    override suspend fun save(tag: Tag): Int {
        dao.upsert(tag)
        return dao.getNewestId()
    }

    override suspend fun delete(id: Int) {
        // タグ削除は論理削除で見た目はタグなしにする。
        val deleteTag = Tag(id = id, tagName = "", icon = R.drawable.bg_pattern2, color = R.color.white, isDeleted = true)
        dao.upsert(deleteTag)
    }

    override suspend fun count(): Int {
        return dao.count()
    }
}
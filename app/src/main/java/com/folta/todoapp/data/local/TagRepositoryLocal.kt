package com.folta.todoapp.data.local

import com.folta.todoapp.R

class TagRepositoryLocal : TagRepository {
    private val dao: TagDAO = MyDataBase.db.tagDAO()

    override suspend fun init() {
        val tags = listOf(
            Tag(
                id = 0,
                tagName = "タグ1",
                pattern = R.drawable.bg_pattern7,
                color = R.color.c1
            )
            ,
            Tag(
                id = 0,
                tagName = "タグ2",
                pattern = R.drawable.bg_pattern2,
                color = R.color.c2
            ),
            Tag(
                id = 0,
                tagName = "タグ3",
                pattern = R.drawable.bg_pattern3,
                color = R.color.c4
            ),
            Tag(
                id = 0,
                tagName = "タグ4",
                pattern = R.drawable.bg_pattern4,
                color = R.color.c6
            ),
            Tag(
                id = 0,
                tagName = "タグ5",
                pattern = R.drawable.bg_pattern5,
                color = R.color.c7
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
        // タグ削除は論理削除で見た目はベタ白にする。
        val deleteTag = Tag(id = id, tagName = "", pattern = R.drawable.bg_pattern1, color = R.color.white, isDeleted = true)
        dao.upsert(deleteTag)
    }

    override suspend fun count(): Int {
        return dao.count()
    }
}
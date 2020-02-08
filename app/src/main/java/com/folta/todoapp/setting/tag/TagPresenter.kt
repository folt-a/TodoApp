package com.folta.todoapp.setting.tag

import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.TagRepository
import com.folta.todoapp.setting.tag.adapter.TagAdapter
import com.folta.todoapp.utility.Const
import com.folta.todoapp.utility.Logger
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.holder_tag.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class TagPresenter
    (
    private val viewTag: TagContract.View,
    private val tagRepos: TagRepository
) : TagContract.Presenter, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val job = Job()

    private lateinit var viewTagList : MutableList<Tag>

    override fun start() {
        launch(Dispatchers.IO) {
            viewTagList = withContext(Dispatchers.Default) {
                tagRepos.getAll().toMutableList()
            }
        }
    }

    override fun addNewTag(fab: FloatingActionButton?) {
        launch(Dispatchers.IO) {
            Logger.d("in IO setOnSafeClickListener ")
            val count = tagRepos.count()
            val tag =
                Tag(
                    id = 0,
                    tagName = "タグ${count + 1}",
                    pattern = Const.tagPatternIdList[Random.nextInt(
                        Const.tagPatternIdList.size
                    )],
                    color = Const.tagColorIdList[Random.nextInt(
                        Const.tagColorIdList.size
                    )]
                )

            val savedId = tagRepos.save(tag)
            val savedTag = tagRepos.find(savedId)
            withContext(Dispatchers.Main) {
                Logger.d("in Main setOnSafeClickListener")
                if (savedTag != null) viewTagList.add(savedTag)
                viewTag.notifyTagAdd()
            }
        }
    }

    override suspend fun fixTag(holder: TagAdapter.TagViewHolder): Tag? {
        val tag = viewTagList.getOrNull(holder.adapterPosition)
        Logger.d("holder.adapterPosition = " + holder.adapterPosition.toString())
        Logger.d("color = " + holder.tagColorSpinner.selectedItem.toString())
        Logger.d("pattern = " + holder.tagPatternSpinner.selectedItem.toString())
        tag?.tagName = holder.tagName.text.toString()
        tag?.color = holder.tagColorSpinner.selectedItem as Int
        tag?.pattern = holder.tagPatternSpinner.selectedItem as Int

        coroutineScope {
            launch(job + Dispatchers.IO) {
                Logger.d("in IO onDoneCheck")
                if (tag != null) tagRepos.save(tag)
            }
        }
        return tag
    }

    override fun deleteTag(pos: Int) {
        launch(job + Dispatchers.IO) {
            // 実データセットからアイテムを削除
            tagRepos.delete(viewTagList[pos].id)
            Logger.d("delete id : ${viewTagList[pos].id}")
            viewTagList.removeAt(pos)
            withContext(Dispatchers.Main) {
                viewTag.notifyTagDelete(pos)
            }
        }
    }

    override fun getTagListSize(): Int {
        return viewTagList.size
    }

    override fun getTagId(position: Int): Long {
        return viewTagList[position].id.toLong()
    }

    override fun getTagByPos(pos: Int): Tag {
        return viewTagList[pos]
    }
}
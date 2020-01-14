package com.folta.todoapp.todo

import android.view.View
import com.folta.todoapp.utility.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.TagRepository
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.data.local.ToDoRepository
import com.folta.todoapp.utility.toStringSlashyyyyMMdd
import com.folta.todoapp.todo.adapter.ToDoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wdullaer.materialdatetimepicker.Utils
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.holder_todo.*
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext

class TodoPresenter(
    private val viewToDo: TodoContract.View,
    private val todoRepos: ToDoRepository,
    private val tagRepos: TagRepository
) : TodoContract.Presenter,
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val job = Job()

    override lateinit var titleDate: LocalDate
    private lateinit var viewToDoList: MutableList<ToDo>
    private lateinit var allTagList: MutableList<Tag>

    override fun start() {
        titleDate = LocalDate.now()

        launch(Dispatchers.IO) {
            viewToDoList = withContext(Dispatchers.Default) {
                Logger.d("in withContext 111 onViewCreated")
                todoRepos.findByDate(titleDate.toStringSlashyyyyMMdd()).toMutableList()
            }

            allTagList = withContext(Dispatchers.Default) {
                Logger.d("in withContext onViewCreated")
                tagRepos.getAll().toMutableList()
            }
            allTagList.add(
                0,
                Tag(
                    id = 0,
                    tagName = "タグなし",
                    pattern = R.drawable.bg_pattern1,
                    color = R.color.white
                )
            )
        }
    }

    override fun addNewToDo(fab: FloatingActionButton) {
        val orderId: Int
        if (viewToDo.todoAdapter.itemCount != 0) {
            orderId = viewToDoList.maxBy { toDo -> toDo.orderId }!!.orderId + 1
        } else {
            orderId = 1
        }

        val todo =
            ToDo(
                id = 0,
                isChecked = false,
                content = "",
                title = "",
                tagId = 0,
                createdAt = titleDate.toStringSlashyyyyMMdd(),
                orderId = orderId
            )
        launch(Dispatchers.IO) {
            Logger.d("in IO fab.setOnSafeClickListener ")

            val savedId = todoRepos.save(todo)
            val savedTodo = todoRepos.find(savedId)
            if (savedTodo != null) viewToDoList.add(savedTodo)

            viewToDo.notifyToDoAdd()
        }
    }

    override suspend fun createDatePicker(): DatePickerDialog {
        val picker = DatePickerDialog.newInstance(
            viewToDo,
            titleDate.year,
            titleDate.monthValue - 1,
            titleDate.dayOfMonth
        )
        val strDates = todoRepos.getExistsDate()
        Logger.d("onClickCalendarOptionMenu highlightedDays $strDates")
        withContext(Dispatchers.Main) {
            if (strDates.isNotEmpty()) {
                val array = arrayOfNulls<Calendar>(strDates.size)
                strDates.forEachIndexed { index, s ->
                    val date = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    val calendar = Calendar.getInstance(TimeZone.getDefault())
                    calendar.set(Calendar.YEAR, date.year)
                    calendar.set(Calendar.MONTH, date.monthValue - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                    Utils.trimToMidnight(calendar)
                    array[index] = calendar
                }
                picker.highlightedDays = array
            }
        }
        return picker
    }

    override fun datePickerSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        titleDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
        launch(Dispatchers.IO) {
            Logger.d("in IO onClickCalendarOptionMenu")
            viewToDoList =
                todoRepos.findByDate(titleDate.toStringSlashyyyyMMdd()).toMutableList()
            withContext(Dispatchers.Main) {
                Logger.d("in withContext onClickCalendarOptionMenu")
                viewToDo.setActionBarTitle(titleDate.toStringSlashyyyyMMdd())
                viewToDo.notifyToDoChanged()
            }
        }
    }

    override fun moveToDo(toPosition: Int, fromPosition: Int) {
        viewToDoList.add(toPosition, viewToDoList.removeAt(fromPosition))
    }

    override fun getToDoListSize(): Int {
        return viewToDoList.size
    }

    override fun getToDoId(position: Int): Long {
        return viewToDoList[position].id.toLong()
    }

    override fun getTagListSize(): Int {
        return allTagList.size
    }

    override fun getTagId(position: Int): Long {
        return getTagByTagPos(position).id.toLong()
    }

    override suspend fun fixToDo(holder: ToDoAdapter.ToDoViewHolder): ToDo? {
        val todo = viewToDoList.getOrNull(holder.adapterPosition)
        todo?.title = holder.title.text.toString()
        todo?.tagId = holder.tagSpinner.selectedItemId.toInt()
        if (holder.content.isEnabled) {
            todo?.content = holder.content.fullText
            Logger.d("content change : " + holder.content.fullText)
        }
        todo?.isChecked = holder.isDone.isChecked

        coroutineScope {
            launch(job + Dispatchers.IO) {
                Logger.d("in IO onDoneCheck")
                if (todo != null) todoRepos.save(todo)
            }
        }
        return todo
    }

    override fun changeTag(v: View?, holder: ToDoAdapter.ToDoViewHolder) {
        launch(job + Dispatchers.IO) {
            Logger.d("in IO onSpinnerSelected")
            fixToDo(holder) ?: return@launch

            val tag = getTagByToDoPos(holder.adapterPosition)

            withContext(Dispatchers.Main) {
                Logger.d("in Main onSpinnerSelected")
                viewToDo.tagDraw(v, tag, holder)
            }
        }
    }

    override fun deleteToDo(holder: ToDoAdapter.ToDoViewHolder) {
        launch(job + Dispatchers.IO) {
            // 実データセットからアイテムを削除
            todoRepos.delete(viewToDoList[holder.adapterPosition].id)
            Logger.d("delete : ${viewToDoList[holder.adapterPosition].id}")
            viewToDoList.removeAt(holder.adapterPosition)
            withContext(Dispatchers.Main) {
                viewToDo.notifyToDoDelete(holder.adapterPosition)
            }
        }
    }

    override fun getMemo(pos: Int): String =
        viewToDoList[pos].content

    override fun getTitle(pos: Int): String = viewToDoList[pos].title
    override fun getChecked(pos: Int): Boolean = viewToDoList[pos].isChecked
    override fun getTagNoByToDoPos(pos: Int): Int {
        val tag = allTagList.firstOrNull { it.id == viewToDoList[pos].tagId }

        if (tag == null || tag.isDeleted) {
            return 0
        }
        return allTagList.indexOf(tag)
    }
    override fun getTagByToDoPos(pos: Int): Tag {
        val tag = allTagList.firstOrNull { it.id == viewToDoList[pos].tagId }

        if (tag == null || tag.isDeleted) {
            return allTagList[0]
        }
        return tag
    }

    override fun getTagByTagPos(tagPos: Int): Tag {
        return allTagList[tagPos]
    }

    override fun saveToDoList() {
        launch(job + Dispatchers.IO) {
            todoRepos.saveSort(viewToDoList)
        }
    }
}
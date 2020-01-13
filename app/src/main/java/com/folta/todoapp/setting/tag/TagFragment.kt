package com.folta.todoapp.setting.tag

import android.content.Context
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.folta.todoapp.utility.Const
import com.folta.todoapp.utility.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.TagRepository
import com.folta.todoapp.utility.TileDrawable
import com.folta.todoapp.utility.setOnSafeClickListener
import com.folta.todoapp.setting.tag.adapter.TagAdapter
import kotlinx.android.synthetic.main.fragment_tag_list.*
import kotlinx.android.synthetic.main.holder_tag.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class TagFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = Job()

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var tagAdapter: TagAdapter
    private val tagRepository by inject<TagRepository>()

    private lateinit var menu: Menu

    private lateinit var viewTagList: MutableList<Tag>

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.menu_tag, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
            R.id.fixTag -> {
                closeKeyboard(view)
            }
            R.id.deleteButton -> {
                onClickDeleteOptionMenu(item)
            }
        }
        return true
    }

    private fun onClickDeleteOptionMenu(menuItem: MenuItem) {
        when (tagAdapter.state) {
            TagAdapter.ListShowState.NORMAL -> {
                tagAdapter.state = TagAdapter.ListShowState.DELETE
                menuItem.setIcon(R.drawable.ic_check)
            }
            else -> {
                tagAdapter.state = TagAdapter.ListShowState.NORMAL
                menuItem.setIcon(R.drawable.ic_trash)
            }
        }
        tagAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tag_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this.context)
        // 区切り線の表示
        recycleView.addItemDecoration(
            DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
        )

        launch(Dispatchers.IO) {
            Logger.d("in IO onViewCreated")

            viewTagList = tagRepository.getAll().toMutableList()

            withContext(Dispatchers.Main) {
                Logger.d("in withContext onViewCreated")
                tagAdapter = object : TagAdapter(viewTagList) {
                    override fun onColorSpinnerSelected(
                        view: View?,
                        id: Int,
                        position: Int,
                        holder: TagViewHolder
                    ) {
                        onSpinnerSelected(view, id, position, holder)
                    }

                    override fun onPatternSpinnerSelected(
                        view: View?,
                        id: Int,
                        position: Int,
                        holder: TagViewHolder
                    ) {
                        onSpinnerSelected(view, id, position, holder)
                    }

                    override fun onSpinnerSelected(
                        v: View?,
                        id: Int,
                        position: Int,
                        holder: TagViewHolder
                    ) {
                        Logger.d("スピナーA onItemSelected id = $id")
                        val tag = getEditedTag(holder)
                        launch(job + Dispatchers.IO) {
                            Logger.d("in IO onSpinnerSelected ")

                            tagRepository.save(tag)

                            withContext(Dispatchers.Main) {
                                Logger.d("in withContext onSpinnerSelected ")
                                Logger.d("pos : " + holder.adapterPosition)
                                Logger.d("tagColor =" + tag.color)
                                Logger.d("tagPattern =" + tag.pattern)
                                // タグ変更されたので描画やりなおし
                                val colorResId = tag.color
                                val patternResId = tag.pattern
                                if (v != null) {
                                    val drawable = TileDrawable.create(
                                        v.context,
                                        colorResId,
                                        patternResId,
                                        Shader.TileMode.REPEAT
                                    )
                                    holder.todoTag.setImageDrawable(drawable)
                                }

                            }
                        }
                    }

                    override fun onTagNameClick(v: View?, holder: TagViewHolder) {
                        openKeyboard(v)
                    }

                    override fun onTagNameEditorAction(
                        v: TextView?,
                        actionId: Int,
                        holder: TagViewHolder
                    ): Boolean {
                        when (actionId) {
                            EditorInfo.IME_ACTION_DONE -> {
                                closeKeyboard(v)
                                return true
                            }
                            else -> {
                                return false
                            }
                        }
                    }

                    override fun onTagNameFocusChange(
                        v: View?,
                        hasFocus: Boolean,
                        holder: TagViewHolder
                    ): Boolean {
                        if (hasFocus) {
                            return holder.tagName.performClick()
                        } else {
                            val tag = getEditedTag(holder)
                            launch(job + Dispatchers.IO) {
                                Logger.d("in IO onTagNameFocusChange ")
                                tagRepository.save(tag)
                            }
                            closeKeyboard(v)
                            return true
                        }
                    }

                    override fun onClickDelete(v: View?, holder: TagViewHolder) {
                        launch(job + Dispatchers.IO) {
                            // 実データセットからアイテムを削除
                            tagRepository.delete(tagAdapter.items[holder.adapterPosition].id)
                            Logger.d("delete : ${tagAdapter.items[holder.adapterPosition].id}")
                            viewTagList.removeAt(holder.adapterPosition)
                            withContext(Dispatchers.Main) {
                                tagAdapter.notifyItemRemoved(holder.adapterPosition)
                            }
                        }
                    }
                }
                tagAdapter.setHasStableIds(true)
                recycleView.adapter = tagAdapter
                tagAdapter.notifyDataSetChanged()
            }
        }

        fab.setOnSafeClickListener {

            launch(Dispatchers.IO) {
                Logger.d("in IO setOnSafeClickListener ")
                val count = tagRepository.count()
                val tag =
                    Tag(
                        id = 0,
                        tagName = "タグ${count + 1}",
                        pattern = Const.tagPatternIdList[Random.nextInt(
                            Const.tagPatternIdList.size)],
                        color = Const.tagColorIdList[Random.nextInt(
                            Const.tagColorIdList.size)]
                    )

                val savedId = tagRepository.save(tag)
                val savedTag = tagRepository.find(savedId)
                withContext(Dispatchers.Main) {
                    Logger.d("in withContext setOnSafeClickListener")
                    if (savedTag != null) viewTagList.add(savedTag)
                    tagAdapter.notifyItemInserted(tagAdapter.itemCount - 1)
                }
            }
        }
    }

    private fun openKeyboard(view: View?) {
        fab.hide()
        menu.findItem(R.id.fixTag).isVisible = true
        menu.findItem(R.id.deleteButton).isVisible = false
        context?.let { ContextCompat.getSystemService(it, InputMethodManager::class.java) }
            ?.showSoftInput(view, 1)
    }

    private fun closeKeyboard(view: View?) {
        coordinatorLayout.requestFocus()
        fab.show()
        menu.findItem(R.id.fixTag).isVisible = false
        menu.findItem(R.id.deleteButton).isVisible = true
        context?.let { ContextCompat.getSystemService(it, InputMethodManager::class.java) }
            ?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_tag_list.*
import kotlinx.android.synthetic.main.holder_tag.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class TagFragment : TagContract.View, Fragment(), CoroutineScope {

    override lateinit var presenter: TagContract.Presenter

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = Job()

    private var listener: OnFragmentInteractionListener? = null

    override lateinit var tagAdapter: TagAdapter
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

        presenter = inject<TagContract.Presenter>(
            "", null
        ) {
            parametersOf(
                this,
                inject<TagRepository>().value
            )
        }.value
        presenter.start()

        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this.context)

        tagAdapter = TagAdapter(this, presenter)
        tagAdapter.setHasStableIds(true)
        recycleView.adapter = tagAdapter
        notifyTagChanged()

        // 区切り線の表示
        recycleView.addItemDecoration(
            DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
        )

        fab.setOnSafeClickListener {
            onClickFab(it)
        }
    }

    /**
     * Fabをクリックしたときのイベント
     *
     * @param fab
     */
    private fun onClickFab(fab: FloatingActionButton?) {
        presenter.addNewTag(fab)
    }

    internal fun onColorSpinnerSelected(
        view: View?,
        id: Int,
        position: Int,
        holder: TagAdapter.TagViewHolder
    ) {
        onSpinnerSelected(view, id, position, holder)
    }

    internal fun onPatternSpinnerSelected(
        view: View?,
        id: Int,
        position: Int,
        holder: TagAdapter.TagViewHolder
    ) {
        onSpinnerSelected(view, id, position, holder)
    }

    private fun onSpinnerSelected(
        v: View?,
        id: Int,
        position: Int,
        holder: TagAdapter.TagViewHolder
    ) {
        launch(job + Dispatchers.IO) {
            Logger.d("in IO onSpinnerSelected $id")
            presenter.fixTag(holder)
            withContext(Dispatchers.Main) {
                Logger.d("in withContext onSpinnerSelected ")

                // タグ変更されたので描画やりなおし
                tagDraw(v, presenter.getTagByPos(holder.adapterPosition), holder)
            }
        }
    }

    internal fun onTagNameClick(v: View?, holder: TagAdapter.TagViewHolder) {
        openKeyboard(v)
    }

    internal fun onTagNameEditorAction(
        v: TextView?,
        actionId: Int,
        holder: TagAdapter.TagViewHolder
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

    internal fun onTagNameFocusChange(
        v: View?,
        hasFocus: Boolean,
        holder: TagAdapter.TagViewHolder
    ): Boolean {
        if (hasFocus) {
            return holder.tagName.performClick()
        } else {
            launch(job + Dispatchers.IO) {
                Logger.d("in IO onContentFocusChange ")
                presenter.fixTag(holder)
            }

            closeKeyboard(v)
            return true
        }
    }

    internal fun onClickDelete(v: View?, holder: TagAdapter.TagViewHolder) {
        presenter.deleteTag(holder.adapterPosition)
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

    override fun tagDraw(v: View?, tag: Tag, holder: TagAdapter.TagViewHolder) {
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    override fun notifyTagChanged() {
        tagAdapter.notifyDataSetChanged()
    }

    override fun notifyTagDelete(pos: Int) {
        tagAdapter.notifyItemRemoved(pos)
    }

    override fun notifyTagAdd() {
        tagAdapter.notifyItemInserted(tagAdapter.itemCount - 1)
    }
}
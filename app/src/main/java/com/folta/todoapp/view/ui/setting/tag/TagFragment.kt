package com.folta.todoapp.view.ui.setting.tag

import android.content.Context
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.folta.todoapp.Const
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.data.local.TagRepository
import com.folta.todoapp.view.ui.TileDrawable
import com.folta.todoapp.view.ui.setOnSafeClickListener

import kotlinx.android.synthetic.main.fragment_tag_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.random.Random

class TagFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null

    private lateinit var tagAdapter: TagAdapter
    private val tagRepository by inject<TagRepository>()

    private lateinit var viewTagList: MutableList<Tag>
    private val job = Job()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tag, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
            R.id.deleteTag -> {

            }
        }
        return true
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
//        recycleView.setOnTouchListener { v, event ->
//            //            Logger.d(event.action.toString())
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                ContextCompat.getSystemService(v.context, InputMethodManager::class.java)
//                    ?.hideSoftInputFromWindow(v?.windowToken, 0)
//                coordinatorLayout.requestFocus()
//            }
//            return@setOnTouchListener true
//        }

        CoroutineScope(Dispatchers.Main + job).launch {
            viewTagList =
                tagRepository.getAll().toMutableList()

            Logger.d(viewTagList.toString())

            tagAdapter = object : TagAdapter(viewTagList) {
                private fun onSpinnerSelected(
                    id: Int,
                    holder: TagViewHolder,
                    v: View?
                ) {
                    Logger.d("スピナー onItemSelected id = $id")
                    val tag = getEditedToDo(holder)
                    CoroutineScope(Dispatchers.Main + job).launch {
                        if (tag != null) {
                            tagRepository.save(tag)
//                            タグ変更されたので描画やりなおし
                            var tag = viewTagList.firstOrNull { it.id == tag.id }
//                            タグなし、削除済みタグは未設定タグとして描画する
                            if (tag == null || tag.isDeleted) {
                                tag = viewTagList[0]
                            }
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
                            getEditedToDo(holder)
                        }
                    }
                }
                override fun onColorSpinnerSelected(v: View?, id: Int, holder: TagViewHolder) {
                    this.onSpinnerSelected(id, holder, v)
                }
                override fun onPatternSpinnerSelected(v: View?, id: Int, holder: TagViewHolder) {
                    this.onSpinnerSelected(id, holder, v)
                }
            }
            tagAdapter.setHasStableIds(true)
            recycleView.adapter = tagAdapter
            tagAdapter.notifyDataSetChanged()
        }

        fab.setOnSafeClickListener {
            val inputMethodManager =
                ContextCompat.getSystemService(it!!.context, InputMethodManager::class.java)
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)

            val tag =
                Tag(
                    id = 0,
                    tagName = "タグ${tagAdapter.itemCount + 1}",
                    pattern = R.drawable.bg_pattern1,
                    color = Const.tagColorIdList[Random.nextInt(Const.tagColorIdList.size)]
                )
            CoroutineScope(Dispatchers.Main + job).launch {
                val savedId = tagRepository.save(tag)
                val savedTag = tagRepository.find(savedId)
                savedTag?.let { it ->
                    viewTagList.add(it)
                    tagAdapter.notifyItemInserted(tagAdapter.itemCount)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
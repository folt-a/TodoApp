package com.folta.todoapp.setting.tag.adapter

import android.graphics.Shader
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.folta.todoapp.utility.Const
import com.folta.todoapp.utility.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.setting.tag.TagContract
import com.folta.todoapp.setting.tag.TagFragment
import com.folta.todoapp.setting.tag.TagPresenter
import com.folta.todoapp.utility.TileDrawable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_tag.*
import kotlinx.android.synthetic.main.holder_tag.view.*

open class TagAdapter(val fragment: TagFragment, val presenter: TagContract.Presenter) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    enum class ListShowState {
        NORMAL,
        DELETE;
    }

    var state: ListShowState =
        ListShowState.NORMAL

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        when (state) {
            ListShowState.DELETE -> holder.bindDelete(position)
            ListShowState.NORMAL -> holder.bindNormal(position)
        }
    }

    private val tagColorAdapter =
        TagColorSpinnerAdapter(Const.tagColorIdList)
    private val tagPatternAdapter =
        TagPatternSpinnerAdapter(Const.tagPatternIdList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_tag, parent, false)
        val holder = TagViewHolder(view)

        //        Spinnerアダプターセット
        holder.tagColorSpinner.adapter = tagColorAdapter
        holder.tagPatternSpinner.adapter = tagPatternAdapter

//        listenerをセットする
        with(holder.itemView) {
            tagName.setOnClickListener { v ->
                fragment.onTagNameClick(v, holder)
            }
            tagName.setOnEditorActionListener { v, actionId, _ ->
                fragment.onTagNameEditorAction(v, actionId, holder)
            }
            tagName.setOnFocusChangeListener { v, hasFocus ->
                if (fragment.onTagNameFocusChange(v, hasFocus, holder)) {
                    return@setOnFocusChangeListener
                }
            }
            deleteButton.setOnClickListener { v ->
                fragment.onClickDelete(v, holder)
            }
        }
        return holder
    }

    override fun getItemCount(): Int = presenter.getTagListSize()

    override fun getItemId(position: Int): Long = presenter.getTagId(position)

    inner class TagViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bindNormal(pos: Int) {
            val tag = presenter.getTagByPos(pos)
            if (tag.isDeleted) return

            fragment.tagDraw(todoTag, tag, this)

            tagName.isEnabled = true
            tagName.setText(tag.tagName)
            // イベントを発火させないためにListenerセットの前に値変更する。
            tagColorSpinner.isEnabled = true
            tagColorSpinner.setSelection(Const.tagColorIdList.indexOf(tag.color), false)
            tagPatternSpinner.isEnabled = true
            tagPatternSpinner.setSelection(Const.tagPatternIdList.indexOf(tag.pattern), false)
            tagPatternSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    //Spinnerのドロップダウンアイテムが選択された時
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        fragment.onPatternSpinnerSelected(
                            view,
                            id.toInt(),
                            position,
                            this@TagViewHolder
                        )
                    }

                    //Spinnerのドロップダウンアイテムが選択されなかった時はなにもなし
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            tagColorSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    //Spinnerのドロップダウンアイテムが選択された時
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        fragment.onColorSpinnerSelected(
                            view,
                            id.toInt(),
                            position,
                            this@TagViewHolder
                        )
                    }

                    //Spinnerのドロップダウンアイテムが選択されなかった時はなにもなし
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            deleteButton.visibility = View.GONE
        }

        fun bindDelete(pos: Int) {
            val tag = presenter.getTagByPos(pos)
            if (tag.isDeleted) return

            fragment.tagDraw(todoTag,tag,this)

            tagName.isEnabled = false
            tagName.setText(tag.tagName)
            tagColorSpinner.isEnabled = false
            tagPatternSpinner.isEnabled = false
            deleteButton.visibility = View.VISIBLE
        }
    }
}

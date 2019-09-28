package com.folta.todoapp.view.ui.setting.tag

import android.graphics.Shader
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.folta.todoapp.Const
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.holder_tag.*
import kotlinx.android.synthetic.main.holder_tag.view.*

open class TagAdapter(var items: List<Tag>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    private val tagColorAdapter = TagColorSpinnerAdapter(Const.tagColorIdList)
    private val tagPatternAdapter = TagPatternSpinnerAdapter(Const.tagPatternIdList)

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
                onTagNameClick(v, holder)
            }
            tagName.setOnEditorActionListener { v, actionId, _ ->
                onTagNameEditorAction(v, actionId, holder)
            }
            tagName.setOnFocusChangeListener { v, hasFocus ->
                if (onTagNameFocusChange(v, hasFocus, holder)) return@setOnFocusChangeListener
            }
        }
        return holder
    }

    open fun onTagNameFocusChange(v: View?, hasFocus: Boolean, holder: TagViewHolder): Boolean {
        return true
    }

    open fun onTagNameEditorAction(v: TextView?, actionId: Int, holder: TagViewHolder): Boolean {
        return true
    }

    open fun onTagNameClick(v: View?, holder: TagViewHolder) {
    }

    open fun onColorSpinnerSelected(view: View?, id: Int, position: Int, holder: TagViewHolder) {
    }

    open fun onPatternSpinnerSelected(view: View?, id: Int, position: Int, holder: TagViewHolder) {
    }

    open fun onSpinnerSelected(v: View?, id: Int, position: Int, holder: TagViewHolder) {}

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    internal fun getEditedTag(holder: TagViewHolder): Tag {
        val item = items[holder.adapterPosition]
        Logger.d("holder.adapterPosition = " + holder.adapterPosition.toString())
        Logger.d("color = " + holder.tagColorSpinner.selectedItem.toString())
        Logger.d("pattern = " + holder.tagPatternSpinner.selectedItem.toString())
        item.tagName = holder.tagName.text.toString()
        item.color = holder.tagColorSpinner.selectedItem as Int
        item.pattern = holder.tagPatternSpinner.selectedItem as Int
        return item
    }

    inner class TagViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(tag: Tag) {
            if (tag.isDeleted) return
            // TODO VectorをRepeat生成する処理キャッシュしたい
            val drawable = TileDrawable.create(
                todoTag.context,
                tag.color,
                tag.pattern,
                Shader.TileMode.REPEAT
            )
            todoTag.setImageDrawable(drawable)
            // Listenerセットの前に値変更 TODO 重いかな？
            tagColorSpinner.setSelection(Const.tagColorIdList.indexOf(tag.color), false)
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
                        onPatternSpinnerSelected(view, id.toInt(), position, this@TagViewHolder)
                    }

                    //Spinnerのドロップダウンアイテムが選択されなかった時
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
                        onColorSpinnerSelected(view, id.toInt(), position, this@TagViewHolder)
                    }

                    //Spinnerのドロップダウンアイテムが選択されなかった時
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            tagName.setText(tag.tagName)
        }
    }
}

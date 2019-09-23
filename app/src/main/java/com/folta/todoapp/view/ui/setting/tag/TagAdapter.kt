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
import kotlinx.android.synthetic.main.holder_tag.view.*
import kotlinx.android.synthetic.main.holder_tag.view.todoTag

open class TagAdapter(var items: List<Tag>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = items[position]

        if (item.isDeleted) return

        val drawable = TileDrawable.create(
            holder.todoTag.context,
            item.color,
            item.pattern,
            Shader.TileMode.REPEAT
        )
        holder.todoTag.setImageDrawable(drawable)

        holder.tagColorSpinner.setSelection(Const.tagColorIdList.indexOf(item.color), false)
        holder.tagPatternSpinner.setSelection(Const.tagPatternIdList.indexOf(item.pattern), false)

        holder.tagName.setText(item.tagName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_tag, parent, false)
        val holder = TagViewHolder(view)

        //        Spinnerアダプターセット
        val spinnerTagColorAdapter = TagColorSpinnerAdapter(Const.tagColorIdList)
        holder.tagColorSpinner.adapter = spinnerTagColorAdapter
        spinnerTagColorAdapter.notifyDataSetChanged()

        val spinnerTagPatternAdapter = TagPatternSpinnerAdapter(Const.tagPatternIdList)
        holder.tagPatternSpinner.adapter = spinnerTagPatternAdapter
        spinnerTagPatternAdapter.notifyDataSetChanged()

//        listenerをセットする
        with(holder.itemView) {
            tagPatternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                //Spinnerのドロップダウンアイテムが選択された時
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onPatternSpinnerSelected(view, id.toInt(), holder)
                }

                //Spinnerのドロップダウンアイテムが選択されなかった時
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            tagColorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                //Spinnerのドロップダウンアイテムが選択された時
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onColorSpinnerSelected(view, id.toInt(), holder)
                }

                //Spinnerのドロップダウンアイテムが選択されなかった時
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
        return TagViewHolder(view)
    }

    open fun onColorSpinnerSelected(view: View?, id: Int, holder: TagViewHolder) {
    }

    open fun onPatternSpinnerSelected(view: View?, id: Int, holder: TagViewHolder) {
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    internal fun getEditedToDo(holder: TagViewHolder): Tag? {
        val item = items.getOrNull(holder.adapterPosition)
        item?.tagName= holder.tagName.text.toString()
        item?.color = holder.tagColorSpinner.selectedItemId.toInt()
        item?.pattern = holder.tagPatternSpinner.selectedItemId.toInt()
        return item
    }

    inner class TagViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val todoTag: ImageView = itemView.todoTag
        val tagName: EditText = itemView.tagName_selected
        val tagPatternSpinner: Spinner = itemView.tagPatternSpinner
        val tagColorSpinner: Spinner = itemView.tagColorSpinner
    }
}

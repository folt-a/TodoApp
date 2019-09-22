package com.folta.todoapp.view.ui.setting.tag

import android.graphics.Shader
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.folta.todoapp.R
import com.folta.todoapp.data.local.Tag
import com.folta.todoapp.view.ui.TileDrawable
import kotlinx.android.synthetic.main.holder_tag.view.*

open class TagAdapter(var items: List<Tag>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {


    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val item = items[position]

        if (item.isDeleted) return

        val todoTagDrawable = ContextCompat.getDrawable(holder.todoTag.context, item.pattern)
        todoTagDrawable?.let {
            it.setTint(holder.todoTag.resources.getColor(item.color))
            holder.todoTag.setImageDrawable(TileDrawable(it, Shader.TileMode.REPEAT))
        }

//        mutate()がなければ自動的に最適化されてすべてのHolderが同じ色、パターンで表示されてしまう
        val tagPatternDrawable = ContextCompat.getDrawable(holder.tagPattern.context, item.pattern)
        tagPatternDrawable?.let { it ->
            it.setTint(holder.todoTag.resources.getColor(R.color.black))
            holder.tagPattern.setImageDrawable(it.mutate())
        }

        val tagColorDrawable = ContextCompat.getDrawable(holder.tagColor.context, R.drawable.bg_pattern1)
        tagColorDrawable?.let { it ->
            it.setTint(holder.todoTag.resources.getColor(item.color))
            holder.tagColor.setImageDrawable(it.mutate())
        }

        holder.tagName.setText(item.tagName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.holder_tag, parent, false)
        val holder = TagViewHolder(view)

//        listenerをセットする
        with(holder.itemView) {

        }
        return TagViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    inner class TagViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.linearLayout
        val todoTag: ImageView = itemView.todoTag
        val tagName: EditText = itemView.tagName
        val tagPattern: ImageView = itemView.tagPattern
        val tagColor: ImageView = itemView.tagColor
    }
}

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
        val todoTagDrawable = ContextCompat.getDrawable(holder.todoTag.context, R.drawable.bg_pattern8)
        todoTagDrawable?.let {
            todoTagDrawable.setTint(holder.todoTag.resources.getColor(R.color.colorSub))
            holder.todoTag.setImageDrawable(TileDrawable(it, Shader.TileMode.REPEAT))
        }



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

    inner class TagViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.linearLayout
        val todoTag: ImageView = itemView.todoTag
        val tagName: EditText = itemView.tagName
        val tagPattern: ImageView = itemView.tagPattern
        val tagColor: ImageView = itemView.tagColor
    }
}

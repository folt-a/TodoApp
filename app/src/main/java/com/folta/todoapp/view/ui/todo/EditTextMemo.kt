package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.folta.todoapp.view.ui.setting.MemoOpen
import com.folta.todoapp.view.ui.setting.Pref

class EditTextMemo(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    var isOpen: Boolean = false
    var fullText: String = this.text.toString()
    var textState: MemoOpen? =  Pref(context).memoOpen

    @SuppressLint("SetTextI18n")
    fun setMemoText(text: String) {
        //      本文は改行削除＋入らない部分は非表示にする
        this.fullText = text

        when {
            textState == MemoOpen.OneLine && this.fullText.length > 20 -> {
                this.setText("${this.fullText.replace("\n", " ").substring(0..20)}...")
                this.visibility = View.VISIBLE
            }
            textState == MemoOpen.AllClose || this.fullText.isEmpty() -> {
                this.visibility = View.GONE
            }

            textState == MemoOpen.OneLine -> {
                this.setText(this.fullText.replace("\n", " "))
                this.visibility = View.VISIBLE
            }
            textState == MemoOpen.AllOpen -> {
                this.setText(this.fullText)
                this.visibility = View.VISIBLE
            }
        }
    }
}
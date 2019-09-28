package com.folta.todoapp.view.ui.todo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import com.folta.todoapp.R
import com.folta.todoapp.view.ui.setting.MemoOpen
import com.folta.todoapp.view.ui.setting.Pref
import kotlinx.android.synthetic.main.holder_todo.view.*

class EditTextMemo(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    var isOpen: Boolean = false
    var fullText: String = this.text.toString()
    private var textState: MemoOpen? = Pref(context).memoOpen

    fun openMemo() {
//        背景、枠
        this.setBackgroundResource(R.drawable.edittext_content)
//        マージン
        val mlp = this.layoutParams
        if (mlp is ViewGroup.MarginLayoutParams) {
            mlp.setMargins(
                this.context.resources.getDimensionPixelSize(R.dimen.dp8),
                this.context.resources.getDimensionPixelSize(R.dimen.dp8),
                0,
                0
            )
        }
//        パディング
        this.setPadding(
            this.context.resources.getDimensionPixelSize(R.dimen.dp8),
            this.context.resources.getDimensionPixelSize(R.dimen.dp8),
            this.context.resources.getDimensionPixelSize(R.dimen.dp8),
            this.context.resources.getDimensionPixelSize(R.dimen.dp8)
        )
//        文字の大きさ
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        this.visibility = View.VISIBLE
        this.visibility = View.VISIBLE
        this.isEnabled = true
        this.isFocusable = true
        this.isFocusableInTouchMode = true

        this.setText(this.fullText)
    }

    fun closeMemo() {
//        背景、枠
        this.background = null
//        マージン
        val mlp = this.layoutParams
        if (mlp is ViewGroup.MarginLayoutParams) {
            mlp.setMargins(
                this.context.resources.getDimensionPixelSize(R.dimen.dp8),
                0,
                0,
                0
            )
        }
//        パディング
        this.setPadding(
            0,
            0,
            0,
            0
        )
//        文字の大きさ
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)

        this.isEnabled = false
        this.isFocusable = false
        this.isFocusableInTouchMode = false

        setClosedMemoText(this.fullText)
        showClosedMemo()
    }

    @SuppressLint("SetTextI18n")
    private fun setClosedMemoText(text: String) {
//      1行メモは改行削除し、入りきらない部分をカットして表示する。
        when {
            textState == MemoOpen.OneLine && text.length > 20 -> {
                this.setText("${text.replace("\n", " ").substring(0..20)}...")
            }
            textState == MemoOpen.OneLine -> {
                this.setText(text.replace("\n", " "))
            }
            textState == MemoOpen.AllOpen -> {
                this.setText(text)
            }
            textState == MemoOpen.AllClose -> {
                this.setText("")
            }
        }
    }

    private fun showClosedMemo() {
//      設定ファイルによる閉じたメモの表示
        when {
            this.fullText.isEmpty() -> {
                this.visibility = View.GONE
            }
            textState == MemoOpen.AllClose -> {
                this.visibility = View.GONE
            }
            textState == MemoOpen.OneLine -> {
                this.visibility = View.VISIBLE
            }
            textState == MemoOpen.AllOpen -> {
                this.visibility = View.VISIBLE
            }
        }
    }
}
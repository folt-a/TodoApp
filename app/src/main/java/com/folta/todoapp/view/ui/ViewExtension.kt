package com.folta.todoapp.view.ui

import android.view.View

private var clickTime: Long = 0

fun <T : View> T.setOnSafeClickListener(block: (T) -> Unit) {
    this.setOnClickListener { view ->
        if (System.currentTimeMillis() - clickTime < 1000) {
            return@setOnClickListener
        }
        @Suppress("UNCHECKED_CAST")
        block(view as T)
        clickTime = System.currentTimeMillis()
    }
}
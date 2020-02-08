package com.folta.todoapp.utility

import com.folta.todoapp.R

class Const {
    companion object {
        // スプラッシュ時間（1000 = 1秒）
        const val SPLASH_TIME: Long = 1500
        val tagColorIdList =
            arrayOf(
                R.color.c1,
                R.color.c2,
                R.color.c3,
                R.color.c4,
                R.color.c5,
                R.color.c6,
                R.color.c7
            )
        val tagPatternIdList = arrayOf(
            R.drawable.bg_pattern1,
            R.drawable.bg_pattern2,
            R.drawable.bg_pattern3,
            R.drawable.bg_pattern4,
            R.drawable.bg_pattern5,
            R.drawable.bg_pattern6,
            R.drawable.bg_pattern7,
            R.drawable.bg_pattern8
        )
    }
}
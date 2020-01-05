package com.folta.todoapp.view.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.folta.todoapp.R

interface Profile {
    var memoOpen: MemoOpen
    val tagColor: TagColor
    val tagPattern: TagPattern
//    var countDebug:Int
}

class Pref(context: Context) : Profile {

    companion object {
        private const val PREF_KEY_MEMO_OPEN = "memoOpen"
        private const val PREF_KEY_TAG_COLOR = "tagColor"
        private const val PREF_KEY_TAG_PATTERN = "tagPattern"
    }

    private val pref: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    override var memoOpen: MemoOpen
        get() = MemoOpen.from(pref.getString(PREF_KEY_MEMO_OPEN, MemoOpen.AllOpen.value))
        set(memoOpen) = pref.edit().putString(PREF_KEY_MEMO_OPEN,memoOpen.value).apply()

    override val tagColor: TagColor
        get() = TagColor.from(pref.getInt(PREF_KEY_TAG_COLOR, TagColor.Color1.value))

    override val tagPattern: TagPattern
        get() = TagPattern.from(pref.getInt(PREF_KEY_TAG_PATTERN, TagPattern.Pattern1.value))

//    override var countDebug: Int
//        get() = pref.getInt("countdebug", 0)
//        set(value) = pref.edit().putInt("countdebug", value).apply()
}

enum class MemoOpen(val value: String) {
    AllOpen("all_open"),
    OneLine("one_line"),
    AllClose("all_close");

    companion object {
        fun from(value: String?): MemoOpen = values().first { value == it.value }
    }
}

enum class TagColor(val value: Int) {
    Color1(R.color.c1),
    Color2(R.color.c2),
    Color3(R.color.c3),
    Color4(R.color.c4),
    Color5(R.color.c5),
    Color6(R.color.c6),
    Color7(R.color.c7);

    companion object {
        fun from(value: Int?): TagColor = values().first { value == it.value }
    }
}

enum class TagPattern(val value: Int) {
    Pattern1(R.drawable.bg_pattern1),
    Pattern2(R.drawable.bg_pattern2),
    Pattern3(R.drawable.bg_pattern3),
    Pattern4(R.drawable.bg_pattern4),
    Pattern5(R.drawable.bg_pattern5),
    Pattern6(R.drawable.bg_pattern6),
    Pattern7(R.drawable.bg_pattern7),
    Pattern8(R.drawable.bg_pattern8);

    companion object {
        fun from(value: Int?): TagPattern = values().first { value == it.value }
    }
}
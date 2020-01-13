package com.folta.todoapp.utility

import android.util.Log
import timber.log.Timber

class Logger {
    companion object {
        fun init() {
            Timber.plant(OriginalTree())
        }

        fun d(message: String, vararg args: Any) {
            Timber.d(message, *args)
        }

        fun i(message: String, vararg args: Any) {
            Timber.i(message, *args)
        }

        fun w(message: String, vararg args: Any) {
            Timber.w(message, *args)
        }

        fun e(message: String, vararg args: Any) {
            Timber.e(message, *args)
        }

        fun e(t: Throwable, message: String, vararg args: Any) {
            Timber.e(t, message, *args)
        }

        fun wtf(t: Throwable, message: String, vararg args: Any) {
            Timber.wtf(t, message, *args)
        }
    }
}

class OriginalTree : Timber.Tree() {

    /**
     * 指定した宛先にログを出力します。
     * @param priority ログレベル。android.util.Log のログレベルが設定される。
     * @param tag 明示タグもしくは推定タグ。Nullの場合もある。
     * @param message フォーマットされたログメッセージ。Timber.d()などでしていたメッセージをフォーマットした内容。
     * @param t 付随する例外。Timber.e()で指定するとここに設定される。
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        // これで簡易のログ出力はできる。
        // 以下で出力したログは、[6: Android Monitor]の"LogCat"で確認することができる。
        Log.println(priority, tag, message)
    }
}
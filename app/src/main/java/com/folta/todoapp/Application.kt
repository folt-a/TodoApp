package com.folta.todoapp

import androidx.core.app.AppLaunchChecker
import androidx.room.Room
import com.folta.todoapp.data.local.*
import com.folta.todoapp.view.ui.setting.MemoOpen
import com.folta.todoapp.view.ui.setting.Pref
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

class Application : android.app.Application() {
    private val job = Job()
    private val module: Module = module {
        single<ToDoRepository> { (ToDoRepositoryLocal()) }
        single<TagRepository> { (TagRepositoryLocal()) }
    }

    override fun onCreate() {
        super.onCreate()

//        起動初期処理
//        ログ
        Logger.init()
//        LocalDate
        AndroidThreeTen.init(this)

//        Koinコンテナ生成
        startKoin(
            this, listOf(
                this.module
            )
        )

        MyDataBase.db =
            Room.databaseBuilder(this.applicationContext, MyDataBase::class.java, "todo").build()

        //        初回起動時のみ初期設定を行う
        if (!AppLaunchChecker.hasStartedFromLauncher(this)) {
            Logger.d("初回起動")

            CoroutineScope(Dispatchers.Main + job).launch {
//                Setting
                Pref(applicationContext).memoOpen = MemoOpen.OneLine
//                デフォルトのタグを追加
                val tagRepository by inject<TagRepository>()
                tagRepository.init()

            }
        }


    }
}
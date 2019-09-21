package com.folta.todoapp

import androidx.room.Room
import com.folta.todoapp.data.local.*
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

//        初回起動時はデフォルトのタグを追加
        val tagRepository by inject<TagRepository>()
        CoroutineScope(Dispatchers.Main + job).launch {
            val tag = tagRepository.count()
            Logger.d("tagCount : $tag")
            if (tag == 0) tagRepository.init()
        }

    }
}
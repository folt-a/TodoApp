package com.folta.todoapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.folta.todoapp.data.local.MyDataBase
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.data.local.ToDoDAO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {


    lateinit var dao: ToDoDAO
    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val db = Room.databaseBuilder(this, MyDataBase::class.java, "todo").build()
        dao = db.todoDAO()

        fab.setOnClickListener { _ ->
            onClick()
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    var printStr: String = ""
    private fun onClick() {
        val todo = ToDo(id = 0, isChecked = false, content = "aaa", title = "title")
        CoroutineScope(Dispatchers.Main + job).launch {
            dao.add(todo)
            val aaa = dao.getAll()

            printStr = aaa.toString()
        }
        println(printStr)
    }

}

package com.folta.todoapp.view

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.folta.todoapp.Const
import com.folta.todoapp.Logger
import com.folta.todoapp.R
import com.folta.todoapp.view.ui.todo.ToDoListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_todo.*

class TodoActivity : AppCompatActivity(), ToDoListFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {

    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()


        window.requestFeature(Window.FEATURE_ACTION_BAR)
        Handler().postDelayed({
            setTheme(R.style.AppTheme)
            setContentView(R.layout.activity_todo)

            navController = findNavController(R.id.nav_host_fragment)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_todo, R.id.navigation_notifications
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            nav_view.setupWithNavController(navController)
//        supportActionBar?.hide()
        }, Const.SPLASH_TIME)
    }

    private fun init() {
        //        起動初期処理
        Logger.init()
        AndroidThreeTen.init(this)
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}

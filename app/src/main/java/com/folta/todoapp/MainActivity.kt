package com.folta.todoapp

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AppLaunchChecker
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.folta.todoapp.utility.Const
import com.folta.todoapp.setting.tag.TagFragment
import com.folta.todoapp.todo.ToDoListFragment
import kotlinx.android.synthetic.main.activity_todo.*

class MainActivity : AppCompatActivity(), ToDoListFragment.OnFragmentInteractionListener ,TagFragment.OnFragmentInteractionListener{
    override fun onFragmentInteraction(uri: Uri) {

    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_ACTION_BAR)

        Handler().postDelayed({
            setTheme(R.style.AppTheme)
            setContentView(R.layout.activity_todo)

            navController = findNavController(R.id.nav_host_fragment)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_todo, R.id.navigation_setting
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            nav_view.setupWithNavController(navController)
            AppLaunchChecker.onActivityCreate(this)
        }, Const.SPLASH_TIME)
    }

    override fun onSupportNavigateUp()
            = navController.navigateUp()

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }
}

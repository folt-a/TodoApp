package com.folta.todoapp.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.folta.todoapp.R
import com.folta.todoapp.data.local.ToDo
import com.folta.todoapp.data.local.ToDoRepository
import com.folta.todoapp.data.local.ToDoRepositoryLocal
import kotlinx.android.synthetic.main.fragment_todo_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ToDoListFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var repository: ToDoRepository

    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.Main + job).launch {
            repository = ToDoRepositoryLocal(view.context)
            val list = repository.findByDate("")
            val adapter = ToDoAdapter(view.context)

            if (!list.isNullOrEmpty()) {
                adapter.addAll(list)
            }
            listToDo.adapter = adapter
        }

        fab.setOnClickListener {
            onClick()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun onClick() {
        val todo = ToDo(id = 0, isChecked = false, content = "aaa", title = "title", createdAt = "")
        CoroutineScope(Dispatchers.Main + job).launch {
            repository.save(todo)
        }
    }
}

package com.example.todolistapp

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivityMainBinding private constructor(
    private val rootView: View,
    val todoRecyclerView: RecyclerView,
    val addTodoButton: FloatingActionButton
) {
    val root: View get() = rootView

    companion object {
        fun inflate(inflater: LayoutInflater): ActivityMainBinding {
            val root = inflater.inflate(R.layout.activity_main, null, false)
            return ActivityMainBinding(
                rootView = root,
                todoRecyclerView = root.findViewById(R.id.todoRecyclerView),
                addTodoButton = root.findViewById(R.id.addTodoButton)
            )
        }
    }
}

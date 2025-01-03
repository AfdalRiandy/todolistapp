package com.example.todolistapp.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.R
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private var todos: MutableList<Todo>,
    private val onTodoChecked: (Todo) -> Unit,
    private val onEditClick: (Todo) -> Unit,
    private val onDeleteClick: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.todoCheckBox)
        val todoText: TextView = view.findViewById(R.id.todoText)
        val dueDateText: TextView = view.findViewById(R.id.dueDateText)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val dueTimeText: TextView = view.findViewById(R.id.dueTimeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        val context = holder.itemView.context

        holder.todoText.text = todo.task
        holder.checkBox.isChecked = todo.isCompleted

        // Set due date text
        todo.dueDate?.let { date ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            holder.dueDateText.text = dateFormat.format(Date(date))

            // Change text color based on due date
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            when {
                date < today -> holder.dueDateText.setTextColor(context.getColor(R.color.red)) // overdue
                date == today -> holder.dueDateText.setTextColor(context.getColor(R.color.orange)) // today
                else -> holder.dueDateText.setTextColor(context.getColor(R.color.gray)) // future
            }

            holder.dueDateText.visibility = View.VISIBLE
        } ?: run {
            holder.dueDateText.visibility = View.GONE // Hide if no due date is set
        }

        // Set due time text
        todo.dueTime?.let { time ->
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.dueTimeText.text = timeFormat.format(Date(time))
            holder.dueTimeText.visibility = View.VISIBLE
        } ?: run {
            holder.dueTimeText.visibility = View.GONE // Hide if no due time is set
        }

        // Change text color if the todo is completed
        if (todo.isCompleted) {
            holder.todoText.setTextColor(context.getColor(android.R.color.darker_gray))
            holder.dueDateText.setTextColor(context.getColor(android.R.color.darker_gray)) // Match due date color
            holder.dueTimeText.setTextColor(context.getColor(android.R.color.darker_gray)) // Match due time color
        } else {
            holder.todoText.setTextColor(context.getColor(R.color.black))
        }

        // Set up listeners
        holder.checkBox.setOnClickListener {
            todo.isCompleted = holder.checkBox.isChecked
            onTodoChecked(todo)
        }

        holder.editButton.setOnClickListener {
            onEditClick(todo)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(todo)
        }
    }


    override fun getItemCount() = todos.size

    fun updateTodos(newTodos: List<Todo>) {
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }
}

package com.example.todolistapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistapp.R
import com.example.todolistapp.database.DatabaseHelper
import com.example.todolistapp.database.Todo
import com.example.todolistapp.database.TodoAdapter
import android.app.AlertDialog
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import android.app.DatePickerDialog
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var todoAdapter: TodoAdapter
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = requireActivity().getSharedPreferences("TodoApp", 0)
            .getInt("USER_ID", -1)

        dbHelper = DatabaseHelper(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadTodos()
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            mutableListOf(),
            onTodoChecked = { todo ->
                dbHelper.updateTodo(todo)
                loadTodos()
            },
            onEditClick = { todo ->
                showAddEditTodoDialog(todo)
            },
            onDeleteClick = { todo ->
                dbHelper.deleteTodo(todo.id, userId)
                loadTodos()
            }
        )

        view?.findViewById<RecyclerView>(R.id.todoRecyclerView)?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    private fun setupClickListeners() {
        view?.findViewById<FloatingActionButton>(R.id.addTodoButton)?.setOnClickListener {
            showAddEditTodoDialog()
        }
    }

    private fun showAddEditTodoDialog(todo: Todo? = null) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_todo, null)

        val todoEditText = dialogView.findViewById<TextInputEditText>(R.id.todoEditText)
        val datePickerButton = dialogView.findViewById<MaterialButton>(R.id.datePickerButton)
        val selectedDateText = dialogView.findViewById<TextView>(R.id.selectedDateText)

        var selectedDate: Long? = todo?.dueDate

        // Function to update the displayed date text
        fun updateDateText() {
            if (selectedDate != null) {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                selectedDateText.text = "Due: ${dateFormat.format(Date(selectedDate!!))}"
                selectedDateText.visibility = View.VISIBLE
            } else {
                selectedDateText.visibility = View.GONE
            }
        }

        todo?.let {
            todoEditText.setText(it.task)
            updateDateText()
        }

        // Open DatePickerDialog to select due date
        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedDate?.let { date ->
                calendar.timeInMillis = date
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    updateDateText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (todo == null) "Add Todo" else "Edit Todo")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val task = todoEditText.text.toString()
                if (task.isNotEmpty()) {
                    if (todo == null) {
                        dbHelper.insertTodo(Todo(userId = userId, task = task, dueDate = selectedDate))
                    } else {
                        dbHelper.updateTodo(todo.copy(task = task, dueDate = selectedDate))
                    }
                    loadTodos()
                } else {
                    Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadTodos() {
        val todos = dbHelper.getAllTodosForUser(userId)
        todoAdapter.updateTodos(todos)
    }
}
